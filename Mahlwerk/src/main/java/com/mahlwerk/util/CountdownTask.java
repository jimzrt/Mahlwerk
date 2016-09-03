package com.mahlwerk.util;

import java.util.TimerTask;

public class CountdownTask extends TimerTask{
	
	
	public int countdown;
	public boolean paused = true;
	public CountdownTask(int countdown){
		//this.countdown = countdown;
	}



    public void run() {
        if(!paused){
        	countdown++;
        if (countdown == 0)
            this.cancel();
        }
    }

	

}
