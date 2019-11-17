import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;

public class TuringMachineRunner {
	
	public static class RunException extends Exception {
		
	}
	
	public static class OutOfFuelException extends RunException {
		
	}
	
	public static class StuckException extends RunException {
		
		TuringMachine.State st;
		Tape[] tapes;
		
		public StuckException(TuringMachine.State st, Tape[] tapes) {
			super();
			this.st = st;
			this.tapes = tapes;
		}

		public TuringMachine.State getState() {
			return st;
		}

		public Tape[] getTapes() {
			return tapes;
		}
		
	}

	public static class Tape {
		
		private ArrayDeque<Character> left;
		private ArrayDeque<Character> right;
		
		public Tape() {
			left = new ArrayDeque<>();
			right = new ArrayDeque<>();
		}
		
		public Tape(String w) {
			left = new ArrayDeque<>();
			right = new ArrayDeque<>();
			for (int i = 0; i < w.length(); i++)
				right.offerLast(w.charAt(i));
		}
		
		public Tape(Tape t) {
			left = new ArrayDeque<>(t.left);
			right = new ArrayDeque<>(t.right);
		}
		
		public char read() {
			if (right.isEmpty())
				return TuringMachine.EMPTY_LETTER;
			else
				return right.getFirst();
		}
		
		public void move(TuringMachine.Direction d) {
			char c;
			switch (d) {
			case L:
				if (left.isEmpty()) {
					right.offerFirst(TuringMachine.EMPTY_LETTER);
				} else {
					c = left.pollLast();
					right.offerFirst(c);
				}
				return;
			case R:
				if (right.isEmpty()) {
					left.offerLast(TuringMachine.EMPTY_LETTER);
				} else {
					c = right.pollFirst();
					left.offerLast(c);
				}
				return;
			case N:
				return;
			}
		}
		
		public void write(char c) {
			if (c != TuringMachine.ANY_LETTER) {
				if (!right.isEmpty())
					right.pollFirst();
				right.addFirst(c);
			}
		}
		
		public String getContents() {
			StringBuilder sb = new StringBuilder();
			for (char c : right)
				sb.append(c);
			return sb.toString();
		}
		
		private static char prettyChar(char c) {
			return (c == TuringMachine.EMPTY_LETTER) ? '□' : c;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("…□□");
			for (char c : left)
				sb.append(prettyChar(c));
			if (right.isEmpty()) {
				sb.append("[□]");
			} else {
				Iterator<Character> it = right.iterator();
				sb.append('[').append(prettyChar(it.next())).append(']');
				while (it.hasNext())
					sb.append(prettyChar(it.next()));
			}
			if (right.size() < 2 || right.getLast() != TuringMachine.EMPTY_LETTER)
				sb.append('□');
			sb.append("□□…");
			return sb.toString();
		}
		
		public String getLeft() {
			StringBuilder sb = new StringBuilder();
			for (char c : left)
				sb.append(c);
			return sb.toString();
		}
		
		public String getRight() {
			StringBuilder sb = new StringBuilder();
			for (char c : right)
				sb.append(c);
			return sb.toString();
		}
		
	}
	
	private int fuel;
	private TuringMachine tm;
	private Tape[] tapes;
	private TuringMachine.State state;
	
	public int getFuel() {
		return fuel;
	}
	
	public TuringMachineRunner(TuringMachine tm, int fuel, Tape... tapes) {
		this.tm = Objects.requireNonNull(tm);
		this.state = Objects.requireNonNull(tm.getInitialState());
		this.tapes = new Tape[tm.numberOfTapes];
		this.fuel = fuel;
		for (int i = 0; i < tapes.length; i++)
			this.tapes[i] = new Tape(Objects.requireNonNull(tapes[i]));
	}
	
	public void applyTransition(TuringMachine.Transition t) throws OutOfFuelException {
		if (fuel == 0)
			throw new OutOfFuelException();
		fuel--;
			
		state = t.successor;
		for (int i = 0; i < tm.numberOfTapes; i++) {
			tapes[i].write(t.letters[i]);
			tapes[i].move(t.directions[i]);
		}
	}
	
	public char[] readTapes() {
		char letters[] = new char[tm.numberOfTapes];
		for (int i = 0; i < tm.numberOfTapes; i++)
			letters[i] = tapes[i].read();
		return letters;
	}
	
	public void run() throws RunException {
		while (!tm.isFinal(state)) {
			TuringMachine.Transition t = tm.getTransition(state, readTapes());
			if (t == null)
				throw new StuckException(state, tapes);
			applyTransition(t);
		}
	}
	
	public Tape[] getTapes() {
		Tape tapes[] = new Tape[tm.numberOfTapes];
		for (int i = 0; i < tm.numberOfTapes; i++)
			tapes[i] = new Tape(this.tapes[i]);
		return tapes;
	}

	public static Tape[] run(TuringMachine tm, int fuel, Tape[] tapes) throws RunException {
		TuringMachineRunner runner = new TuringMachineRunner(tm, fuel, tapes);
		runner.run();
		return runner.getTapes();
	}
	
}
