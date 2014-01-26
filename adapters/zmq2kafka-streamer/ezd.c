/*
 * Copyright (c) 2013, Steve Morin <steve@demandcube.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


#define _ISOC99_SOURCE  /* for strtoull() */
#define _GNU_SOURCE     /* for strdupa() */

#include <string.h>
#include <strings.h>
#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <ctype.h>
#include <stdlib.h>
#include <time.h>
#include <limits.h>
#include <sys/file.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <assert.h>

static char ezd_pidfile_path[PATH_MAX];
static int  ezd_pidfile_fd = -1;


int ezd_str_tof (const char *val) {
	char *end;
	int i;
	
	i = strtoul(val, &end, 0);
	if (end > val) /* treat as integer value */
		return !!i;

	if (!strcasecmp(val, "yes") ||
	    !strcasecmp(val, "true") ||
	    !strcasecmp(val, "on"))
		return 1;
	else
		return 0;
}



/*
 * Left and right trim string '*sp' of white spaces (incl newlines).
 */
static int ezd_trim (char **sp, char *end) {
	char *s = *sp;

	while (s < end && isspace(*s))
		s++;

	end--;

	while (end > s && isspace(*end)) {
		*end = '\0';
		end--;
	}

	*sp = s;

	return (int)(end - *sp);
}


int ezd_csv2array (char ***arrp, const char *valstr) {
	char *val = strdupa(valstr);
	int size = 0;
	int cnt = 0;

	*arrp = NULL;

	while (*val) {
		int len;
		char *t = NULL;
		char *s = val;

		do {
			if (!(t = strchr(s, ','))) {
				/* End of line */
				t = val + strlen(val);
			} else if (t == val)
				break;
			else if (*(t-1) == '\\') {
				/* Escaped: remove the escaper and keep going */
				memmove(t-1, t, strlen(t)+1);
				s = t;
				t = NULL;
			} else
				break;
		} while (!t);

		ezd_trim(&val, t);

		len = (int)(t-val);
		if (len > 0) {
			if (cnt == size) {
				size = (size + 4) * 2;
				*arrp = realloc(*arrp, sizeof(**arrp) * size);
			}

			(*arrp)[cnt++] = strndup(val, len);
		} else
			len++;

		val += len;
	}
	return cnt;
}





int ezd_conf_file_read (const char *path,
			int (*conf_set_cb) (const char *name,
					    const char *val,
					    char *errstr,
					    size_t errstr_size,
					    const char *path,
					    int line,
					    void *opaque),
			char *errstr, size_t errstr_size,
			void *opaque) {
	FILE *fp;
	char buf[512];
	int line = 0;

	if (!(fp = fopen(path, "r"))) {
		snprintf(errstr, errstr_size,
			 "Failed to open configuration file %s: %s",
			 path, strerror(errno));
		return -1;
	}

	while (fgets(buf, sizeof(buf), fp)) {
		char *s = buf;
		char *t;

		line++;

		while (isspace(*s))
			s++;

		if (!*s || *s == '#')
			continue;

		/* "name=value"
		 * find ^      */
		if (!(t = strchr(s, '='))) {
			snprintf(errstr, errstr_size,
				 "%s:%i: warning: "
				 "missing '=': line ignored\n",
				 path, line);
			continue;
		}

		/* ezd_trim "name"=.. */
		if (!ezd_trim(&s, t)) {
			snprintf(errstr, errstr_size,
				 "%s:%i: warning: empty left-hand-side\n",
				 path, line);
			continue;
		}

		/* terminate "name"=.. */
		*t = '\0';
		t++;

		/* ezd_trim ..="value" */
		ezd_trim(&t, t + strlen(t));

		/* set the configuration value. */
		if (conf_set_cb(s, *t ? t : NULL, errstr, errstr_size,
				path, line, opaque) == -1) {
			fclose(fp);
			return -1;
		}
	}


	fclose(fp);
	return 0;
}


void ezd_pidfile_close (void) {
	if (!*ezd_pidfile_path)
		return;

	flock(ezd_pidfile_fd, LOCK_UN|LOCK_NB);
	unlink(ezd_pidfile_path);
	close(ezd_pidfile_fd);
}



int ezd_pidfile_open (const char *path, char *errstr, size_t errstr_size) {
	int fd;
	pid_t currpid = 0;
	char buf[64];
	int r;

	fd = open(path, O_RDWR|O_CREAT, 0644);
	if (fd == -1) {
		snprintf(errstr, errstr_size,
			 "Unable to open pidfile %s: %s",
			 path, strerror(errno));
		return -1;
	}

	/* Read current pid, if any. */
	if ((r = read(fd, buf, sizeof(buf)-1)) > 0) {
		char *end;
		buf[r] = '\0';
		currpid = strtoul(buf, &end, 10);
		if (end == buf)
			currpid = 0;
	}

	if (flock(fd, LOCK_EX|LOCK_NB) == -1) {
		if (errno == EWOULDBLOCK)
			snprintf(errstr, errstr_size,
				 "Pidfile %s locked by other process (%i)",
				 path, (int)currpid);
		else
			snprintf(errstr, errstr_size,
				 "Failed to lock pidfile %s: %s",
				 path, strerror(errno));
		close(fd);
		return -1;
	}

	(void)lseek(fd, 0, SEEK_SET);
	(void)ftruncate(fd, 0);

	snprintf(buf, sizeof(buf), "%i\n", (int)getpid());
	r = write(fd, buf, strlen(buf));
	if (r == -1) {
		snprintf(errstr, errstr_size,
			 "Failed to write pidfile %s: %s",
			 path, strerror(errno));
		close(fd);
		return -1;
	} else if (r < strlen(buf)) {
		snprintf(errstr, errstr_size,
			 "Partial pidfile write %s: %i/%i bytes written",
			 path, r, (int)strlen(buf));
		close(fd);
		return -1;
	}

	strncpy(ezd_pidfile_path, path, sizeof(ezd_pidfile_path)-1);
	ezd_pidfile_fd = fd;

	return ezd_pidfile_fd;
}



enum {
	EZD_DAEMON_WAIT,
	EZD_DAEMON_FAILED,
	EZD_DAEMON_DIED,
	EZD_DAEMON_STARTED,
} ezd_daemon_status = EZD_DAEMON_WAIT;

static void ezd_daemon_sig_started_cb (int sig) {
	/* Child process is now fully started. */
	ezd_daemon_status = EZD_DAEMON_STARTED;
}

static void ezd_daemon_sig_chld_cb (int sig) {
	int st;
	waitpid(-1, &st, 0);
	ezd_daemon_status = EZD_DAEMON_DIED;
}

int ezd_daemon (int timeout_sec, char *errstr, size_t errstr_size) {
	pid_t pid;
	sighandler_t sh_usr2_orig, sh_chld_orig;
	time_t timeout_abs;


	/* Parent process will wait for signal or termination of the
	 * child thread. */
	sh_usr2_orig = signal(SIGUSR2, ezd_daemon_sig_started_cb);
	sh_chld_orig = signal(SIGCHLD, ezd_daemon_sig_chld_cb);
	timeout_abs = time(NULL) + timeout_sec;

	if ((pid = fork()) == -1) {
		snprintf(errstr, errstr_size,
			 "Daemon fork failed: %s", strerror(errno));
		return -1;
	}

	if (pid == 0) {
		/* Child process. */
		signal(SIGUSR2, sh_usr2_orig);
		signal(SIGCHLD, sh_chld_orig);
		return 0;
	}


	while (ezd_daemon_status == EZD_DAEMON_WAIT) {
		usleep(100000);
		if (time(NULL) >= timeout_abs) {
			snprintf(errstr, errstr_size,
				 "Daemon child process (pid %i) did not "
				 "start in %i seconds",
				 (int)pid, timeout_sec);
			kill(pid, SIGTERM);
			ezd_daemon_status = EZD_DAEMON_FAILED;
			signal(SIGUSR2, sh_usr2_orig);
			signal(SIGCHLD, sh_chld_orig);
			return -1;
		}
	}

	signal(SIGUSR2, sh_usr2_orig);
	signal(SIGCHLD, sh_chld_orig);

	if (ezd_daemon_status == EZD_DAEMON_DIED) {
		snprintf(errstr, errstr_size,
			 "Daemon child process (pid %i) terminated "
			 "during startup", (int)pid);
		return -1;
	} else if (ezd_daemon_status == EZD_DAEMON_STARTED)
		exit(0);

	assert(!*"notreached");
	exit(0);
}


void ezd_daemon_started (void) {
	int i;
	close(STDIN_FILENO);
	close(STDOUT_FILENO);
	close(STDERR_FILENO);
	for (i = 0 ; i < 3 ; i++)
		open("/dev/null", 0);
	kill(getppid(), SIGUSR2);
	setsid();
}
