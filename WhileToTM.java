import java.awt.Taskbar.State;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

public class WhileToTM implements WhileProgram.Visitor<TuringMachine> {

	public static final Set<Character> ALPHABET = Set.of('X');
	
	/*
	 * The blank symbol is written as ' '. There is also a special wildcard symbol '*' that
	 * abbreviates "any letter" or "don't change the letter", depending on the context.
	 * 
	 * CONVENTION: Transitions are of the form q, c_1 ... c_n / c_1' ... c_n', D1 ... Dn
	 * 
	 * q is the state in which the automaton must be for the transition to fire.
	 * 
	 * The c_i are the letters that must be under the tape heads for the transition to fire.
	 * If c_i == '*', c_i can be any letter for tape i.
	 * 
	 * q' is the successor state.
	 * 
	 * The c_i' are the letters that are written to the tape. If c_i' == '*', the letter on
	 * tape i  remains unchanged.
	 * 
	 * The D_i are the directions in which the tape heads are moved.
	 * 
	 * 
	 * When creating transitions, you can make things easier for yourself by using the functions
	 * "makeLetters"/"makeDirections" below and the constants "DONT_MOVE" etc.
	 * 
	 */
	
	/**
	 * Abbreviations to make the notation more concise.
	 */
	private static final TuringMachine.Direction N = TuringMachine.Direction.N;
	private static final TuringMachine.Direction R = TuringMachine.Direction.R;
	private static final TuringMachine.Direction L = TuringMachine.Direction.L;
	
	/**
	 *  Builds a vector of letters (f(1), ..., f(n))
	 *  E.g. makeLetters(i -> (i == 0) ? 'a' : 'b')
	 */
	public char[] makeLetters(Function<Integer, Character> f) {
		char[] a = new char[numberOfTapes];
		for (int i = 0; i < numberOfTapes; i++)
			a[i] = f.apply(i);
		return a;
	}

	/**
	 *  Builds a vector of Directions (f(1), ..., f(n))
	 *  E.g. makeDirections(i -> (i == 0) ? R : N)
	 */
	public TuringMachine.Direction[] makeDirections(Function<Integer, TuringMachine.Direction> f) {
		TuringMachine.Direction[] a = new TuringMachine.Direction[numberOfTapes];
		for (int i = 0; i < numberOfTapes; i++)
			a[i] = f.apply(i);
		return a;
	}

	/**
	 * Match on any letter
	 */
	public final char[] ANYTHING;
	
	/**
	 * Don't change any letters
	 */
	public final char[] NO_CHANGE;
	
	/**
	 * Don't move any tape
	 */
	public final TuringMachine.Direction[] DONT_MOVE;
	
	

	/**
	 * The number of tapes to be used (you can assume that this is strictly greater than the index
	 * of any variable appearing in the program)
	 */
	private int numberOfTapes;

	
	//
	// BEGIN IMPLEMENTATION
	//
	
	/**
	 * Returns a TM that does nothing.
	 */
	private TuringMachine idle() {
		return TuringMachine.idle(ALPHABET, numberOfTapes);
	}

	/**
	 * Returns a TM that deletes the content of the given tape, i.e. the tape is completely blank
	 * afterwards. Precondition: the tape is either completely empty or its content is of the form 
	 * "XXX...XXX" and the tape head is on the first X.
	 */
	private TuringMachine deleteTape(int i) {
		TuringMachine.State q0 = new TuringMachine.State();
		TuringMachine.State q1 = new TuringMachine.State();
		TuringMachine m = new TuringMachine(ALPHABET, q0, numberOfTapes);
		m.addFinalState(q1);
		
		// If there's an X on tape i, delete it and move tape i to the right.
		m.addTransition(q0, 
				m.makeLetters(j -> (i == j) ? 'X' : '*'), 
				q0, 
				m.makeLetters(j -> (i == j) ? ' ' : '*'),
				m.makeDirections(j -> (i == j) ? R : N));
		
		// If there's a blank on tape i, we're done and can stop.
		m.addTransition(q0, 
				m.makeLetters(j -> (i == j) ? ' ' : '*'), 
				q1, 
				m.NO_CHANGE, 
				m.DONT_MOVE);
		return m;
	}

	/**
	 * Returns a TM that copies the content of the source tape to the given destination tape,
	 * which is assumed to be completely blank. The source tape is assumed to be either completely empty or
	 * of the form "XXX...XXX" with the tape head on the first X, and after the copying, this is again the case
	 * for both tapes.
	 */
	private TuringMachine copyTape(int src, int dest) {
		
		if(src == dest)
			return idle();
		
		TuringMachine.State q0 = new TuringMachine.State();
		TuringMachine.State q1 = new TuringMachine.State();
		TuringMachine.State q2 = new TuringMachine.State();
		TuringMachine m = new TuringMachine(ALPHABET, q0, numberOfTapes);
		m.addFinalState(q2);
		
		// If there's an X on tape src, copy it to tape dest, move both tapes to the right
		m.addTransition(q0, 
				m.makeLetters(i -> (i == src) ? 'X' : '*'), 
				q0, 
				m.makeLetters(i -> (i == dest) ? 'X' : '*'), 
				m.makeDirections(i -> (i == dest || i == src) ? R : N));

		// If there's a blank letter on tape src, transition into q1
		m.addTransition(q0, 
				m.makeLetters(i -> (i == src) ? ' ' : '*'), 
				q1, 
				m.makeLetters(i -> (i == dest) ? ' ' : '*'), 
				m.makeDirections(i -> (i == src || i == dest) ? L : N));
		
		// Move both of the tape heads to the beginning
		m.addTransition(q1, 
				m.makeLetters(i -> (i == src || i == dest) ? 'X' : '*'), 
				q1, 
				m.NO_CHANGE, 
				m.makeDirections(i -> (i == src || i == dest) ? L : N));
		
		// When both tape heads reach the empty letter at the beginning, stop
		m.addTransition(q1, 
				m.makeLetters(i -> (i == src || i == dest) ? ' ' : '*'), 
				q2, 
				m.NO_CHANGE, 
				m.makeDirections(i -> (i == src || i == dest) ? R : N));
		
		TuringMachine result = deleteTape(dest);
		result.compose(m);
		
		return result;
	}

	/**
	 * Returns a TM that increments the number of the given tape by the given amount
	 */
	private TuringMachine incrementTape(int i, int amount) {
		
		if(amount == 0)
			return idle();
		
		TuringMachine m = null;
				
		for(int k=0; k<amount; k++) 
		{
			TuringMachine.State q0 = new TuringMachine.State();
			TuringMachine.State q1 = new TuringMachine.State();
			TuringMachine.State q2 = new TuringMachine.State();
			TuringMachine incByOne = new TuringMachine(ALPHABET, q0, numberOfTapes);
			incByOne.addFinalState(q2);
			
			incByOne.addTransition(q0, 
					incByOne.ANYTHING, 
					q1, 
					incByOne.NO_CHANGE, 
					incByOne.makeDirections(j -> (j == i) ? L : N));
			
			incByOne.addTransition(q1, 
					incByOne.ANYTHING, 
					q2, 
					incByOne.makeLetters(j -> (j == i) ? 'X' : '*'), 
					incByOne.DONT_MOVE);
						
			if(k == 0)
				m = incByOne;
			else
				m.compose(incByOne);
		}		
		
		return m;
	}

	/**
	 * Returns a TM that decrements the number of the given tape by the given amount
	 */
	private TuringMachine decrementTape(int i, int amount) {
		
		if(amount == 0)
			return idle();
				
		TuringMachine m = null;
		
		
		for(int k=0; k<amount; k++)
		{
			TuringMachine.State q0 = new TuringMachine.State();
			TuringMachine.State q1 = new TuringMachine.State();
			TuringMachine decByOne = new TuringMachine(ALPHABET, q0, numberOfTapes);
			decByOne.addFinalState(q1);
			
			decByOne.addTransition(q0, 
					ANYTHING, 
					q1, 
					decByOne.makeLetters(j -> (j == i) ? ' ' : '*'), 
					decByOne.makeDirections(j -> (j == i) ? R : N));
			
			if(k == 0)
				m = decByOne;
			else
				m.compose(decByOne);
		}
		
		
		return m; 
	}

	@Override
	public TuringMachine visitIncrement(int dest, int src, int amount) {

		TuringMachine m = copyTape(src, dest);
		m.compose( incrementTape(dest, amount) );
		
		return m; 
	}

	@Override
	public TuringMachine visitDecrement(int dest, int src, int amount) {
		
		TuringMachine m = copyTape(src, dest);
		m.compose( decrementTape(dest, amount) );
		
		return m; 
	}

	@Override
	public TuringMachine visitBlock(List<WhileProgram> parts) {
		
		if(parts.isEmpty())
			return idle();
		
		TuringMachine m = null;
		
		for(WhileProgram part : parts) {
			if( m == null )
				m = part.accept(new WhileToTM(numberOfTapes));
			else
				m.compose(part.accept(new WhileToTM(numberOfTapes)));
		}
		
		return m;
	}

	@Override
	public TuringMachine visitIfThenElse(int testVar, WhileProgram thenBody, WhileProgram elseBody) {
		
		TuringMachine.State q0 = new TuringMachine.State();
		TuringMachine m = new TuringMachine(ALPHABET, q0, numberOfTapes);
		
		TuringMachine thenMachine = thenBody.accept(new WhileToTM(numberOfTapes));
		TuringMachine elseMachine = elseBody.accept(new WhileToTM(numberOfTapes));
		
		m.addMachine(thenMachine);
		m.addMachine(elseMachine);
		
		m.addTransition(q0, 
				m.makeLetters(j -> (j == testVar) ? ' ' : '*'), 
				thenMachine.getInitialState(), 
				m.NO_CHANGE, 
				m.DONT_MOVE);
		
		m.addTransition(q0, 
				m.makeLetters(j -> (j == testVar) ? 'X' : '*'), 
				elseMachine.getInitialState(), 
				m.NO_CHANGE, 
				m.DONT_MOVE);
		
		return m; 
	}

	@Override
	public TuringMachine visitWhile(int testVar, WhileProgram loopBody) {
		
		TuringMachine.State q0 = new TuringMachine.State();
		TuringMachine m = new TuringMachine(ALPHABET, q0, numberOfTapes);
		
		TuringMachine loopBodyTM = loopBody.accept(new WhileToTM(numberOfTapes));;
		m.addMachine( loopBodyTM );
		
		for(TuringMachine.State s : m.getFinalStates())
			m.addTransition(s, ANYTHING, q0, NO_CHANGE, DONT_MOVE);
		
		m.addTransition(q0, 
				m.makeLetters(j -> (j == testVar) ? 'X' : '*'), 
				loopBodyTM.getInitialState(), 
				m.NO_CHANGE, 
				m.DONT_MOVE);
		
		TuringMachine.State q1 = new TuringMachine.State();
		Set<TuringMachine.State> finalStates = new HashSet<>();
		finalStates.add(q1);
		m.setFinalStates(finalStates);
		
		m.addTransition(q0, 
				m.makeLetters(j -> (j == testVar) ? ' ' : '*'), 
				q1, 
				m.NO_CHANGE, 
				m.DONT_MOVE);
		
		return m; 
	}
	
	
	//
	// END IMPLEMENTATION
	//
	

	private WhileToTM(int numberOfTapes) {
		this.numberOfTapes = numberOfTapes;
		ANYTHING = makeLetters(i -> '*');
		NO_CHANGE = makeLetters(i -> '*');
		DONT_MOVE = makeDirections(i -> N);
	}

	public static TuringMachine run(WhileProgram p) {
		return p.accept(new WhileToTM(p.getVariableCount()));
	}

	public static int readInt(Scanner sc, String name) throws IOException {
		if (!sc.hasNextLine()) {
			System.err.println(String.format("Invalid input: expected '%s:', but got end of file.", name));
			System.exit(-1);
			return -1;
		}
		String line = sc.nextLine();
		if (!line.startsWith(name + ":")) {
			System.err.println(String.format("Invalid input: expected '%s:', but got %s.", name, line));
			System.exit(-1);
			return -1;
		} else {
			String s = line.substring(name.length() + 1).trim();
			try {
				Integer n = Integer.parseInt(s);
				return n;
			} catch (NumberFormatException e) {
				System.err.println(String.format("Invalid input: not an integer: %s", s));
				System.exit(-1);
				return -1;
			}
		}
	}

	public static String readRemaining(Scanner sc) {
		StringBuilder sb = new StringBuilder();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.equals("EOF"))
				break;
			sb.append(line).append("\n");
		}
		sc.close();
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		int fuel = readInt(sc, "Max fuel");
		int range = readInt(sc, "Variable range");
		if (fuel <= 0 || range <= 0) {
			System.err.println("Invalid input: fuel/variable range must be positive.");
			return;
		}

		try {
			WhileProgram p = WhileProgram.parse(readRemaining(sc));
			TuringMachine tm = WhileToTM.run(p);
			System.out.println(tm);			
		} catch (WhileProgramParser.ParserException e) {
			System.err.println("Failed to parser WHILE program:");
			System.err.println(e.offset(2).getMessage());
		}
			
		sc.close();
	}

}
