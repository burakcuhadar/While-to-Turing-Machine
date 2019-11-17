# While to Turing Machine
In this project I have implemented the procedure to turn a "while-language" in the given format into a Turing machine.
This project was my homework for the [Introduction to Theoretical Computer Science](https://www21.in.tum.de/teaching/theo/SS19/)
course taught at Technical University of Munich.

**NOTE:** Example inputs can be found at the [tests folder](https://github.com/burakcuhadar/While-to-Turing-Machine/tree/master/tests).

**Input Format:** 
1. row: Maximum number of steps the Turing machine is allowed to take. Syntax for that is "Max fuel: x" where x is a number.
2. row: Upper bound for value of variables. Syntax for that is "Variable range: x" where x is a number.
3. row and the following rows: Code written in the while-language whose syntax is given as the following:
* Increment:  **x<sub>i</sub> := x<sub>j</sub> + c**
* Decrement:  **x<sub>i</sub> := x<sub>j</sub> - c**
* Block:  **p<sub>1</sub> ; . . . ; p<sub>k</sub>**
* If then else: **IF x<sub>i</sub> = 0 THEN p<sub>t</sub> ELSE <sub>p</sub> e END**
* While: **WHILE x<sub>i</sub> != 0 DO p END**

**Output Format:**
1. row: Alphabet of the Turing machine
2. row: Start state of the machine
3. row: Terminal states of the machine
4. row: Number of tapes 
5. row and the following rows: arbitrary number of rows describing transitions. 
The syntax for a transition is "q;ab;q’;cd;D<sub>1</sub>D<sub>2</sub>", where:
* q is the source state,
* q’ is the successor state 
* a is the symbol read from the first tape
* b is the symbol read from the second tape 
* c is the symbol written to the first tape
* d is the symbol written to the second tape
* D<sub>1</sub> is the direction the head at the first tape takes
* D<sub>2</sub> is the direction the head at the second tape takes. 
Directions can be one of "L" for left, "R" for right, and "N" for not moving.

The last row: "END" denoting the end of the output.

**Example Input and corresponding Output:**
```
Input:
Max fuel: 1500
Variable range: 5
IF x0 = 0 THEN
x1 := x1 + 1
ELSE
x1 := x1 - 1
END
EOF

Output:
X
54
57;59
2
54; *;55;**;NN
54;X*;58;**;NN
55;**;56;**;NL
56;**;57;*X;NN
58;**;59;* ;NR
END
```
