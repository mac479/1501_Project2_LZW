import java.util.Arrays;

/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt > output.txt  (compress)
 *  Execution:    java LZW + < input.txt > output.txt  (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
	//Easier to edit if they're finals.
	private static final int CODEBOOK_MAX = 65536;
	private static final double MONITOR_RATIO = 1.1;
	
    private static int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width

	private static void resetCompress() { 
    	BinaryStdOut.write('r', 8);
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
        
        while (input.length() > 0) {
        	
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            
            //Run reset on codebook
            if(code == CODEBOOK_MAX) {
            	L = 512;
            	W = 9;
            	code = R+1;
            	st = new TST<>();
                for (int i = 0; i < R; i++)
                    st.put("" + (char) i, i);
            }
            
            //Moved on the chance the code was reset.
        	if(code == L && W < 16) {
        		//If it hits the max for the current number of codewords increases codeword with by 1.
        		W++;
        		L = (int)Math.pow(2, W);
        	}
            
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void resetExpand() {
        String[] st = new String[L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
        	
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L-1) st[i++] = val + s.charAt(0);
            val = s;
            
            //Detects a reset.
            if(i == CODEBOOK_MAX - 1) {
            	
            	W=9;
            	L=512;
            	st=new String[L];
                for (i = 0; i < R; i++)
                    st[i] = "" + (char) i;
                st[i++] = "";                        // (unused) lookahead for EOF
                
                BinaryStdOut.write(val);
                
                codeword = BinaryStdIn.readInt(W);
                if (codeword == R) return;           // expanded message is empty string
                val = st[codeword];
            }

        	if(i == L-1 && W < 16) {
        		
        		W++;
        		L = (int)Math.pow(2, W);
        		//Resizes input array
        		st = Arrays.copyOf(st, L);
        	}
            
        }
        BinaryStdOut.close();
    }
    
    /*
     * Define the compression ratio to be the size of the uncompressed data that has been processed/generated so 
     * far divided by the size of the compressed data generated/processed so far (for compression/expansion, 
     * respectively)
     */
    
    public static void monitorCompress() { 
    	BinaryStdOut.write('m', 8);
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
        
        int uncompressed=0, compressed=0;
        double initialRatio=-1, currentRatio=0;	//-1 is a flag to identify it hasn't been set yet.
        
        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            /*
            if(s.equals(""+(char)97+(char)116+(char)13+(char)10+(char)105))
            	System.err.println("\13 \10 \105"+" "+count);
            */
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            
            
            //Moved on the chance the code was reset.
            //Run reset on codebook
            if(code == CODEBOOK_MAX) {
            	//Updates ratio.
            	uncompressed += (t * 8);
            	compressed += W;
            	currentRatio = (double)uncompressed / (double)compressed;
            	if(initialRatio == -1) {
            		initialRatio=currentRatio;
            	}

            	if(initialRatio/currentRatio > MONITOR_RATIO) {
            		System.err.println(initialRatio/currentRatio);
                	//Reset codebook
                	L = 512;
                	W = 9;
                	st = new TST<Integer>();
                    for (int i = 0; i < R; i++)
                        st.put("" + (char) i, i);
                	code = R+1;
                    
                    //Reset ratio calculations.
                	uncompressed = 0;
                	compressed = 0;
                    initialRatio = -1;
                    
                    
            	}
            }
        	if(code == L && W < 16) {
        		//If it hits the max for the current number of codewords increases codeword with by 1.
        		W++;
        		L = (int)Math.pow(2, W);
        	}
            
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
    
    public static void monitorExpand() {
    	int count=0;
        String[] st = new String[L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        
        int uncompressed=0, compressed=0;
        double initialRatio=-1, currentRatio=0;	//-1 is a flag to identify it hasn't been set yet.

        while (true) {
        	count++;
        	BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            
            String s = st[codeword];
            if (i == codeword) {
            	s = val + val.charAt(0);   // special case hack
            }
            if (i < L-1) {
            	if(i==65535)
            		System.err.println(val + s.charAt(0));
            	st[i++] = val + s.charAt(0);
            }
            val = s;
            //Detects a reset.
            if(i == CODEBOOK_MAX - 1) {
            	uncompressed += (val.length() * 8);
            	compressed += W;
            	currentRatio = (double)uncompressed / (double)compressed;
            	if(initialRatio == -1) {
            		initialRatio=currentRatio;
            	}

            	if(initialRatio/currentRatio > MONITOR_RATIO) {
                	//Reset codebook
            		System.err.println("---------------Reset------------");
                	W=9;
                	L=512;
                	st=new String[L];
                    for (i = 0; i < R; i++)
                        st[i] = "" + (char) i;
                    st[i++] = "";                        // (unused) lookahead for EOF
                    
                    BinaryStdOut.write(val);
                    
                    codeword = BinaryStdIn.readInt(W);
                    if (codeword == R) return;           // expanded message is empty string
                    val = st[codeword];
                    
                    //Reset ratio calculations.
                    uncompressed = 0;
                    compressed = 0;
                    initialRatio = -1;
            	}
            }
        	if(i == L-1 && W < 16) {
        		
        		W++;
        		L = (int)Math.pow(2, W);
        		//Resizes input array
        		st = Arrays.copyOf(st, L);
        	}
            
        }
        BinaryStdOut.close();
    }

    
    public static void compress() { 
    	BinaryStdOut.write('n', 8);
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF
        
        while (input.length() > 0) {
        	if(code == L && W < 16) {
        		//If it hits the max for the current number of codewords increases codeword with by 1.
        		W++;
        		L = (int)Math.pow(2, W);
        	}
        	
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        String[] st = new String[L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true) {
        	if(i == L-1 && W < 16) {
        		W++;
        		L = (int)Math.pow(2, W);
        		//Resizes input array
        		st = Arrays.copyOf(st, L);
        	}
        	
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if(args[0].equals("-")) {
        	if(args[1].equals("r")) {
        		resetCompress();
        	}
        	else if(args[1].equals("m")) {
        		monitorCompress();
        	}
        	else if(args[1].equals("n")) {
            	compress();
        	}
            else throw new IllegalArgumentException("Illegal command line argument: Missing codebook method.");
        }
        else if (args[0].equals("+")) {
        	char method=BinaryStdIn.readChar();
        	//err will not output to the file.
        	switch(method){
        		case 'r':
        			resetExpand();
        			break;
        		case 'm':
        			monitorExpand();
        			break;
        		case 'n':
        			expand();
        			break;
        		default:
        			throw new IllegalArgumentException("Invalid file: Missing specified compression method.");
        	}
        }
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
