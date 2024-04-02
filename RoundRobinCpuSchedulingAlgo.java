//TODO handle edge case where there are no processes that arrival at time 0

import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;

public class RRAlgo{
	static final int TIMEQUANTUM = 4;
	
	static Queue<Process> queue = new LinkedList<Process>();
	static ArrayList<ProcessCreationInfo> processCreationInfoList = new ArrayList<ProcessCreationInfo>();	
	static boolean loadingProcesses = true;
	static boolean executingProcesses = true;

	static Scanner scan = new Scanner(System.in);
	
	static String processExecutionLog = "|";
	static String processTimeLog = 0 + "";
	static int timeLog = 0;
	
	static boolean firstIteration = true;

	public static class Process{
		int id, burstTime, arrivalTime;
		
		public Process(int id, int burstTime, int arrivalTime){
			this.id = id;
			this.burstTime = burstTime;
			this.arrivalTime = arrivalTime;
		}
	}	
	
	// This method is used to format the ProcessTimeLog string for better presentation
	public static String updateProcessTimeLog(int timeLog){
		if (timeLog > 9){
			return "-----" + timeLog; 
		}
		else if(timeLog > 99){
			return "----" + timeLog;
		}
		else{
			return "------" + timeLog;
		}
	}
	
	public static class SchedulerThread implements Runnable{
		//creates processes and loads them onto queue
		Timer timer = new Timer();
		int clock = 0;

		public void startTimer(){
			int delay = 1000;
			int period = 1000;
			timer.scheduleAtFixedRate(new TimerTask(){ public void run(){ clock++; }}, delay, period);
		}
		
		@Override
		public void run(){
			this.startTimer();
			
			while(loadingProcesses){
				if (!loadingProcesses) break;
				
				for(int i = 0; i < processCreationInfoList.size(); i++) {
					ProcessCreationInfo ci = processCreationInfoList.get(i);
					queue.add(new Process(ci.id, ci.burstTime, ci.arrivalTime));
					processCreationInfoList.remove(ci);
				}
					
				if (processCreationInfoList.isEmpty()) {
					loadingProcesses = false;
					timer.cancel();
				}
				
			}
		}

	}

	public static class ExecutionThread implements Runnable{
		//executes processes processes that are on the queue
		@Override
		public void run(){
			while(executingProcesses) {
				Process ps = queue.remove();
					
				if (timeLog > 0) firstIteration = false;

				if ((ps.arrivalTime > 0) && (firstIteration)) {			
					timeLog += ps.arrivalTime;              	
                                	processTimeLog += updateProcessTimeLog(timeLog);	
					processExecutionLog += "      |";
				}
				else if (ps.arrivalTime > timeLog) {
					if (queue.size() >= 1) {
						queue.add(ps);
						continue;
					}
					else {
						timeLog += Math.min(ps.arrivalTime - timeLog, TIMEQUANTUM);
						processTimeLog += updateProcessTimeLog(timeLog);	
						processExecutionLog += "      |";
						queue.add(ps);
						continue;
					}
				}
	
				processExecutionLog += "  P" + ps.id + "  |";
					
				if (ps.burstTime > TIMEQUANTUM) {
					timeLog += TIMEQUANTUM;
					processTimeLog += updateProcessTimeLog(timeLog);							

					ps.burstTime -= TIMEQUANTUM;
					queue.add(ps);
				}
				else{
					timeLog += ps.burstTime;
					processTimeLog += updateProcessTimeLog(timeLog);	

					ps.burstTime = 0;
				}	
				
				if (queue.size() == 0) executingProcesses = false;
			}
		}
	
	}

	public static class ProcessCreationInfo implements Comparable<ProcessCreationInfo>{
		int id, burstTime, arrivalTime;
		
		public ProcessCreationInfo(int id, int burstTime, int arrivalTime){
			this.id = id;
			this.burstTime = burstTime;
			this.arrivalTime = arrivalTime;
		}
		public int compareTo(ProcessCreationInfo o){
			return Integer.compare(this.arrivalTime, o.arrivalTime);
		}
	}
	
	// Creates Process Creation Info objects using information provided by user
	public static ProcessCreationInfo getInfoFromUser(){
		int id, burstTime, arrivalTime;
		
		System.out.print("\nPid: ");	
		id = scan.nextInt();		
		
		System.out.print("Burst Time: ");		
		burstTime = scan.nextInt();
		
		System.out.print("Arrival Time: ");
		arrivalTime = scan.nextInt();

		return new ProcessCreationInfo(id, burstTime, arrivalTime);		
	}


	public static void main(String[] args) throws Exception{		
		System.out.println("\nEnter Process Information");
		
		processCreationInfoList.add(getInfoFromUser());	
		processCreationInfoList.add(getInfoFromUser());			
	
		//close scanner after use
		scan.close();
		
		//sort processCreationInfoList by arrival time
		Collections.sort(processCreationInfoList);

		//create scheduler thread
		SchedulerThread sThread = new SchedulerThread();
		Thread schedulerThread = new Thread(sThread);

		System.out.println("\nLoading processes onto ready queue...");
		schedulerThread.start();		
		schedulerThread.join();
		
		//create executor thread
		ExecutionThread eThread = new ExecutionThread();
		Thread executionThread = new Thread(eThread);
		
		System.out.println("\nBeginning process execution...");
		executionThread.start();
		executionThread.join();

		//print results to user
		System.out.println("\nResults:\n");
		System.out.println(processExecutionLog);
		System.out.println(processTimeLog + "\n");			
	}								
		
}
