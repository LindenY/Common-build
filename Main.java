package main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ca.uwaterloo.asw4j.Balancer;
import ca.uwaterloo.asw4j.Combiner;
import ca.uwaterloo.asw4j.ConcurrentMapDataStore;
import ca.uwaterloo.asw4j.DataStore;
import ca.uwaterloo.asw4j.Instruction;
import ca.uwaterloo.asw4j.InstructionResolver;
import ca.uwaterloo.asw4j.SimpleInstructionResolver;
import ca.uwaterloo.asw4j.ThreadPoolWorkerManager;
import ca.uwaterloo.asw4j.WorkerManager;
import ca.uwaterloo.asw4j.reflection.TypeToken;

public class Main {

	public static void main(String[] args) {

		Integer[] initData = new Integer[] { 0,  50000000
				};

		cancelExecution(initData);
	}

	static void singltThreadExecution(Integer[] data) {

		LoopInstruction instruction = new LoopInstruction();
		
		Long st = System.currentTimeMillis();
		List<String> resultList = instruction.execute(data);
		System.out.println(String.format(
				"NumOfThread: %d, ExecutionTime: %d; ResultSize:%d", 1,
				System.currentTimeMillis() - st, resultList.size()));
	}

	static void multipleThreadsExecution(Integer[] data, int numThread) {

		DataStore dataStore = new ConcurrentMapDataStore();
		dataStore
				.registerBalancer(TypeToken.get(data.getClass()), new Mapper());
		dataStore.registerCombiner(TypeToken.get(ArrayList.class),
				new CollectionCombiner());
		dataStore.add(data);

		InstructionResolver instructionResolver = new SimpleInstructionResolver(
				dataStore);
		instructionResolver.registerInstructionClass(LoopInstruction.class);

		WorkerManager workerManager = new ThreadPoolWorkerManager(numThread, numThread,
				instructionResolver);

		Long st = System.currentTimeMillis();
		List resultList = workerManager.start(ArrayList.class, null);

		System.out.println(String.format(
				"NumOfThread: %d, ExecutionTime: %d; ResultSize:%d", numThread,
				System.currentTimeMillis() - st, resultList.size()));
	}
	
	static void cancelExecution(Integer[] data) {
		
		DataStore dataStore = new ConcurrentMapDataStore();
		dataStore
				.registerBalancer(TypeToken.get(data.getClass()), new Mapper());
		dataStore.registerCombiner(TypeToken.get(ArrayList.class),
				new CollectionCombiner());
		dataStore.add(data);

		InstructionResolver instructionResolver = new SimpleInstructionResolver(
				dataStore);
		instructionResolver.registerInstructionClass(LoopInstruction.class);

		WorkerManager workerManager = new ThreadPoolWorkerManager(3, 3,
				instructionResolver);

		Long st = System.currentTimeMillis();
		Future<ArrayList> future = workerManager.asyncStart(ArrayList.class, null);

		future.cancel(true);
		
		System.out.println("IsDone: " + future.isDone());
		System.out.println("IsCanceled: " + future.isCancelled());
		try {
			System.out.println(String.format(
					"NumOfThread: %d, ExecutionTime: %d; ResultSize:%d", 3,
					System.currentTimeMillis() - st, future.get() == null ? 0 : future.get().size()));
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	static void printResult(List<String> strList) {
		System.out.println();
		System.out.println("-------------Start Print Result " + strList.size()
				+ "----------------");
		for (String str : strList) {
			System.out.println(str);
		}
		System.out.println("-------------End Print Result----------------");
	}

	static void printLeftOver(DataStore dataStore) {
		System.out.println();
		System.out.println("-------------Start Print LeftOver----------------");
		if (dataStore instanceof ConcurrentMapDataStore) {
			Map<TypeToken<?>, List<Object>> map = ((ConcurrentMapDataStore) dataStore)
					.getDataMap();
			for (TypeToken<?> tk : map.keySet()) {
				System.out.println(String.format("[%s] size=%d", tk.toString(),
						map.get(tk).size()));
			}
		}
		System.out.println("-------------End Print LeftOver----------------");
	}

	public static class LoopInstruction extends
			Instruction<Integer[], List<String>> {

		@Override
		public List<String> execute(Integer[] requireData) {
			List<String> strList = new ArrayList<String>();
			for (int i = requireData[0]; i < requireData[1]; i++) {	
				String str = String.valueOf(i);
				int length = str.length() - 1;

				boolean p = true;
				for (int j = 0; j <= Math.floor(length / 2); j++) {
					if (str.charAt(j) != str.charAt(length - j)) {
						p = false;
						break;
					}
				}

				if (p) {
					strList.add(str);
				}
			}

			return strList;
		}
	}

	public static class Mapper implements Balancer<Integer[]> {

		@Override
		public Collection<Integer[]> balance(Collection<Integer[]> collection) {
			Integer[] data = null;
			for (Integer[] d : collection) {
				data = d;
			}

			List<Integer[]> resultCollection = new ArrayList<Integer[]>();
			int period = (int) Math.ceil((data[1] - data[0]) / 3);
			int start = data[0];

			while (start < data[1]) {
				Integer[] snippet = new Integer[2];
				snippet[0] = start;
				snippet[1] = Math.min(start + period, data[1]);
				resultCollection.add(snippet);
				start += period;
			}

			return resultCollection;
		}
	}

	public static class CollectionCombiner implements Combiner<List<String>> {

		@Override
		public List<String> combine(Collection<List<String>> collection) {
			List<String> stringList = new ArrayList<String>();

			for (List<String> strs : collection) {
				stringList.addAll(strs);
			}

			return stringList;
		}
	}

}
