package com.mahlwerk.util;

import java.util.TimerTask;

/**
 * Simple Task that counts up every second
 * @author James
 *
 */
public class CountSecondsTask extends TimerTask {

	public int countdown;
	public boolean paused = true;

	@Override
	public void run() {
		if (!paused) {
			countdown++;
			if (countdown == 0)
				this.cancel();
		}
	}

}
