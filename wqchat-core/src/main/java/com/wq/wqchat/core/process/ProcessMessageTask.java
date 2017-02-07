package com.wq.wqchat.core.process;




public class ProcessMessageTask {
	
	private static ProcessMessageTask I;
	
    public static ProcessMessageTask I() {
        if (I == null) {
            synchronized (ProcessMessageTask.class) {
                if (I == null) {
                    I = new ProcessMessageTask();
                }
            }
        }
        return I;
    }
    
    private ProcessMessageTask (){
    	
    }
}
