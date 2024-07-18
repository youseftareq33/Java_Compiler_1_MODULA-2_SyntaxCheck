package A_RunApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Driver_Test {

	// for data
	static ArrayList<String> al_non_terminals=new ArrayList<>();
	static ArrayList<String> al_terminals=new ArrayList<>();
	static HashMap<String,ArrayList<ArrayList<String>>> hm_grammar_rules=new HashMap<>();
	static HashMap<Integer,ArrayList<String>> hm_production_number=new HashMap<>();
	static String ll1_parse_table[][]=null;
	
	static String arr_reserved_word[]= {"module", "begin", "const", "var", "integer", "real", "char", "procedure", "mod", "div", "readint", "readreal", "readchar", "readln", "writeint", "writereal", "writechar", "writeln", "if", "then", "end", "else", "while", "do", "end", "loop", "until", "exit", "call"};
	static String arr_operator[]= {"<", "<=", ">", ">=", "=", "|=", "!", "=", ":=", "+", "-", "*", "/", ";", ",", ":", ".", "(", ")"};
	
	static String reservedWords[] = {
	        "while", "mod", "char", "*", "readint", ">", "begin", ".", "until", "integer", "<",
	        "writechar", "then", "exit", "end", "readln", "div", "-", "integer-value", "<=",
	        "writereal", "module", ";", "do", "|=", "const", ")", "readchar", "if", ":=",
	        "=", "writeln", "procedure", "real-value", "writeint", ":", "name", "else", "call",
	        "+", "var", "(", "real", "/", "readreal", "loop", ">="
	 };
	
	static List<String> reservedWordsList = List.of(reservedWords);
	
	static ArrayList<String> al_code_string=new ArrayList<>();
	static ArrayList<String> al_code_no_filter=new ArrayList<>();
	static HashMap<Integer,ArrayList<String>> hm_code_line=new HashMap<>();
	
	static Stack<String> stack=new Stack<>();
	
	static int num_line_error=1;
	static String string_error="";
	
	
	
	public static void main(String[] args) {
		read_grammer_file(new File("src\\B_StoreData_FileData\\grammar.txt"));
		build_ll1_parse_table();	
		
		read_modula2_code_file(new File("src\\D_StoreData_TestData_CorrectCode\\testcase_1.txt"));
		
		check_code_syntax();
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////
	// Methods:
	
	//-- Read non_terminals and terminals and grammar rules
	public static void read_grammer_file(File file) {
		// check if the file exist
		if(!file.exists()) {
			System.out.println("there isn't grammer file !!!");
		}
		else {
			
			try {
				
				//////////////////////////*** Read Non-Terminals ***//////////////////////////
				
				// get data thats inside the file.
				Scanner in = new Scanner(file);
				String s_insideLine[] = null;
				
				// get non-terminals
				for(int i=0;in.hasNextLine();i++) {
					String line=in.nextLine().trim(); // store line while trim(remove first and end spaces)
					s_insideLine=line.split("\\s+"); // take the line as tokens
					al_non_terminals.add(s_insideLine[0]); // s_insideLine[0] : non-terminal        token(1)
				}
				
				// print non-terminals
				System.out.println("non-terminals: "+al_non_terminals.toString());
				System.out.println("---------------------------------------------------------------------");
				
				in.close();
				
	            
				
				//////////////////////////*** Read Terminals and Grammar Rules***//////////////////////////
	            in = new Scanner(file);
	            s_insideLine = null;

	            /*
	             * s_insideLine[0] : non-terminal                    token(1)
	             * s_insideLine[1] : arrow(-->)                      token(2)
	             * s_insideLine[2...n] : terminal and non-terminals  token(3)
	             */

	            // get terminals
	            for(int i=0;in.hasNextLine();i++) {
	                String line = in.nextLine().trim(); // store line while trim(remove first and end spaces)
	                s_insideLine = line.split("\\s+"); // take the line as tokens
	                
	                ArrayList<ArrayList<String>> al_all_terminals_one_line = new ArrayList<>(); // groups of terminals
	                ArrayList<String> al_terminals_one_line = new ArrayList<>(); // terminals

	                for(int j=2;j<s_insideLine.length;j++) {
	                	
	                	// if the tokens equal "|"
	                    if (!s_insideLine[j].equals("|")) {
	                    	// if the token doesn't equal non-terminals and doesn't exist before on al_terminals and it's not lambda
                            if (!al_non_terminals.contains(s_insideLine[j]) && !al_terminals.contains(s_insideLine[j]) && !s_insideLine[j].equals("lambda")) {
                            	al_terminals.add(s_insideLine[j]);
                            }
                            
	                        al_terminals_one_line.add(s_insideLine[j]);
	                    } 
	                    // else the tokens doesn't equal "|" 
	                    else {
	                        al_all_terminals_one_line.add(new ArrayList<>(al_terminals_one_line));
	                        al_terminals_one_line.clear();
	                    }
	                }

	                // add last group of terminals after "|"
	                if (!al_terminals_one_line.isEmpty()) {
	                    al_all_terminals_one_line.add(new ArrayList<>(al_terminals_one_line));
	                }

	                hm_grammar_rules.put(s_insideLine[0], al_all_terminals_one_line); // non_terminals --> groups of terminals
	                
	                for(int k=0;k<al_all_terminals_one_line.size();k++) {
	                	if(hm_production_number.isEmpty()) {
                        	hm_production_number.put(1, al_all_terminals_one_line.get(k));
                        }
                        else {
                        	hm_production_number.put(hm_production_number.size()+1, al_all_terminals_one_line.get(k));
                        }
	                }
	            }
	            
	            // print terminals
	            System.out.println("terminals: "+al_terminals.toString());
				System.out.println("---------------------------------------------------------------------");
				
				// print grammar rules 
				System.out.println("grammar rules:"+"\n");
	            for(int i=0;i<al_non_terminals.size();i++) {
	    			System.out.println(al_non_terminals.get(i).toString()+" --> "+hm_grammar_rules.get(al_non_terminals.get(i)));
	    		}
	            
	            System.out.println("---------------------------------------------------------------------");
	            System.out.println("production number: "+hm_production_number.toString());
	            System.out.println("\n\n");
	            
			}
			catch(Exception e) {
				e.getMessage();
			}
			
		}
	}
	
	//-- Build LL(1) Parse Table
	public static void build_ll1_parse_table() {
		// set entry for table:
		ll1_parse_table=new String[al_non_terminals.size()+1][al_terminals.size()+1];
		
		for(int i=0;i<ll1_parse_table.length;i++) {
			for(int j=0;j<ll1_parse_table[i].length;j++) {
				
				// for index [0][0]
				if(i==0 && j==0) {
					ll1_parse_table[0][0]="Vn/Vt";
				}
				// for Vt
				else if(i==0 && j>0) {
					ll1_parse_table[0][j]=al_terminals.get(j-1);
				}
				// for Vn
				else if(i>0 && j==0) {
					ll1_parse_table[i][0]=al_non_terminals.get(i-1);
					
				}
				else {
					ll1_parse_table[i][j]=" ";
				}
				
			}
		}
		
		
		// set production number inside the LL(1) parse table
		ll1_parse_table[1][3]="1"; // module-decl , module
		
		ll1_parse_table[2][3]="2"; // module-heading, module
		
		ll1_parse_table[3][5]="3"; // block, begin
		
		ll1_parse_table[4][5]="4"; // declarations, begin
		ll1_parse_table[4][7]="4"; // declarations, const
		ll1_parse_table[4][9]="4"; // declarations, var
		ll1_parse_table[4][15]="4"; // declarations, procedure
		
		ll1_parse_table[5][5]="6"; // const-decl, begin
		ll1_parse_table[5][7]="5"; // const-decl, const
		ll1_parse_table[5][9]="6"; // const-decl, var
		ll1_parse_table[5][15]="6"; // const-decl, procedure
		
		ll1_parse_table[6][5]="8"; // const-list, begin
		ll1_parse_table[6][15]="8"; // const-list, procedure
		ll1_parse_table[6][1]="7"; // const-list, name
		ll1_parse_table[6][9]="8"; // const-list, var
		
		ll1_parse_table[7][5]="10"; // var-decl, begin
		ll1_parse_table[7][15]="10"; // var-decl, procedure
		ll1_parse_table[7][9]="9"; // var-decl, var
		
		ll1_parse_table[8][5]="12"; // var-list, begin
		ll1_parse_table[8][15]="12"; // var-list, procedure
		ll1_parse_table[8][1]="11"; // var-list, name
		
		ll1_parse_table[9][1]="13"; // var-item, name
		
		ll1_parse_table[10][1]="14"; // name-list, name
		
		ll1_parse_table[11][11]="15"; // more-names, ","
		ll1_parse_table[11][18]="16"; // more-names, ")"
		ll1_parse_table[11][10]="16"; // more-names, ":"
		
		ll1_parse_table[12][14]="19"; // data-type, char
		ll1_parse_table[12][12]="17"; // data-type, integer
		ll1_parse_table[12][13]="18"; // data-type, real
		
		ll1_parse_table[13][5]="21"; // procedure-decl, begin
		ll1_parse_table[13][15]="20"; // procedure-decl, procedure
		
		ll1_parse_table[14][15]="22"; // procedure-heading, procedure
		
		ll1_parse_table[15][36]="23"; // stmt-list, while
		ll1_parse_table[15][25]="23"; // stmt-list, readint
		ll1_parse_table[15][5]="23"; // stmt-list, begin
		ll1_parse_table[15][39]="24"; // stmt-list, until
		ll1_parse_table[15][31]="23"; // stmt-list, writechar
		ll1_parse_table[15][40]="23"; // stmt-list, exit
		ll1_parse_table[15][6]="24"; // stmt-list, end
		ll1_parse_table[15][28]="23"; // stmt-list, readln
		ll1_parse_table[15][30]="23"; // stmt-list, writereal
		ll1_parse_table[15][4]="23"; // stmt-list, ";"
		ll1_parse_table[15][27]="23"; // stmt-list, readchar
		ll1_parse_table[15][33]="23"; // stmt-list, if
		ll1_parse_table[15][32]="23"; // stmt-list, writeln
		ll1_parse_table[15][29]="23"; // stmt-list, writeint
		ll1_parse_table[15][1]="23"; // stmt-list, name
		ll1_parse_table[15][35]="24"; // stmt-list, else
		ll1_parse_table[15][41]="23"; // stmt-list, call
		ll1_parse_table[15][26]="23"; // stmt-list, readreal
		ll1_parse_table[15][38]="23"; // stmt-list, loop
		
		ll1_parse_table[16][36]="29"; // statement, while
		ll1_parse_table[16][25]="26"; // statement, readint
		ll1_parse_table[16][5]="33"; // statement, begin
		ll1_parse_table[16][31]="27"; // statement, writechar
		ll1_parse_table[16][40]="31"; // statement, exit
		ll1_parse_table[16][28]="26"; // statement, readln
		ll1_parse_table[16][30]="27"; // statement, writereal
		ll1_parse_table[16][4]="34"; // statement, ";"
		ll1_parse_table[16][27]="26"; // statement, readchar
		ll1_parse_table[16][33]="28"; // statement, if
		ll1_parse_table[16][32]="27"; // statement, writeln
		ll1_parse_table[16][29]="27"; // statement, writeint
		ll1_parse_table[16][1]="25"; // statement, name
		ll1_parse_table[16][41]="32"; // statement, call
		ll1_parse_table[16][26]="26"; // statement, readreal
		ll1_parse_table[16][38]="30"; // statement, loop
		
		ll1_parse_table[17][1]="35"; // ass-stmt, name
		
		ll1_parse_table[18][47]="36"; // exp, integer-value
		ll1_parse_table[18][48]="36"; // exp, real-value
		ll1_parse_table[18][1]="36"; // exp, name
		ll1_parse_table[18][17]="36"; // exp, "("
		
		ll1_parse_table[19][20]="37"; // exp-prime, "-"
		ll1_parse_table[19][4]="38"; // exp-prime, ";"
		ll1_parse_table[19][18]="38"; // exp-prime, ")"
		ll1_parse_table[19][19]="37"; // exp-prime, "+"
		
		ll1_parse_table[20][47]="39"; // term, integer-value
		ll1_parse_table[20][48]="39"; // term, real-value
		ll1_parse_table[20][1]="39"; // term, name
		ll1_parse_table[20][17]="39"; // term, "("
		
		ll1_parse_table[21][23]="40"; // term-prime, mod
		ll1_parse_table[21][21]="40"; // term-prime, "*"
		ll1_parse_table[21][24]="40"; // term-prime, div
		ll1_parse_table[21][20]="41"; // term-prime, "-"
		ll1_parse_table[21][4]="41"; // term-prime, ";"
		ll1_parse_table[21][18]="41"; // term-prime, ")"
		ll1_parse_table[21][19]="41"; // term-prime, "+"
		ll1_parse_table[21][22]="40"; // term-prime, "/"
		
		ll1_parse_table[22][47]="43"; // factor, integer-value
		ll1_parse_table[22][48]="43"; // factor, integer-value
		ll1_parse_table[22][1]="43"; // factor, name
		ll1_parse_table[22][17]="42"; // factor, "("
		
		ll1_parse_table[23][20]="45"; // add-oper, "-"
		ll1_parse_table[23][19]="44"; // add-oper, "+"
		
		ll1_parse_table[24][23]="48"; // mul-oper, mod
		ll1_parse_table[24][21]="46"; // mul-oper, "*"
		ll1_parse_table[24][24]="49"; // mul-oper, div
		ll1_parse_table[24][22]="47"; // mul-oper, "/"
		
		ll1_parse_table[25][25]="50"; // read-stmt, readint
		ll1_parse_table[25][28]="53"; // read-stmt, readln
		ll1_parse_table[25][27]="52"; // read-stmt, readchar
		ll1_parse_table[25][26]="51"; // read-stmt, readreal
		
		ll1_parse_table[26][31]="56"; // write-stmt, writechar
		ll1_parse_table[26][30]="55"; // write-stmt, writereal
		ll1_parse_table[26][32]="57"; // write-stmt, writeln
		ll1_parse_table[26][29]="54"; // write-stmt, writeint
		
		ll1_parse_table[27][47]="58"; // write-list, integer-value
		ll1_parse_table[27][48]="58"; // write-list, real-value
		ll1_parse_table[27][1]="58"; // write-list, name
		
		ll1_parse_table[28][11]="59"; // more-write-value, ","
		ll1_parse_table[28][18]="60"; // more-write-value, ")"
		
		ll1_parse_table[29][47]="62"; // write-item, integer-value
		ll1_parse_table[29][48]="62"; // write-item, real-value
		ll1_parse_table[29][1]="61"; // write-item, name
		
		ll1_parse_table[30][33]="63"; // if-stmt, if
		
		ll1_parse_table[31][6]="65"; // else-part, end
		ll1_parse_table[31][35]="64"; // else-part, else
		
		ll1_parse_table[32][36]="66"; // while-stmt, while
		
		ll1_parse_table[33][38]="67"; // loop-stmt, loop
		
		ll1_parse_table[34][40]="68"; // exit-stmt, exit
		
		ll1_parse_table[35][41]="69"; // call-stmt, call
		
		ll1_parse_table[36][47]="70"; // condition, integer-value
		ll1_parse_table[36][48]="70"; // condition, real-value
		ll1_parse_table[36][1]="70"; // condition, name
		
		ll1_parse_table[37][45]="75"; // relational-oper, ">"
		ll1_parse_table[37][43]="73"; // relational-oper, "<"
		ll1_parse_table[37][44]="74"; // relational-oper, "<="
		ll1_parse_table[37][42]="72"; // relational-oper, "|="
		ll1_parse_table[37][8]="71"; // relational-oper, "="
		ll1_parse_table[37][46]="76"; // relational-oper, ">="
		
		ll1_parse_table[38][47]="78"; // name-value, integer-value
		ll1_parse_table[38][48]="78"; // name-value, real-value
		ll1_parse_table[38][1]="77"; // name-value, name
		
		ll1_parse_table[39][47]="79"; // value, integer-value
		ll1_parse_table[39][48]="80"; // value, real-value
		
	}

	//-- Read modula2 code
	public static void read_modula2_code_file(File file) {
	    // Check if the file exists
	    if (!file.exists()) {
	        System.out.println("there isn't grammar file !!!");
	    } else {
	        try {
	            // Get data that's inside the file
	            Scanner in = new Scanner(file);
	            int i = 0;
	            System.out.println("=====================================================================");
	            System.out.println("The code: " + "\n\n");

	            while (in.hasNextLine()) {
	                String line = in.nextLine().trim();
	                ArrayList<String> al_code = new ArrayList<>();
	                System.out.println(line);

	                if (!line.isEmpty()) {
	                	String[] tokens_line = line.split("(?<=\\W)|(?=\\W)|\\s+");
	                	ArrayList al_token=new ArrayList<>();
	                	String realValue="";
	                	for(int h=0;h<tokens_line.length;h++) {
	                		
	                		if(!tokens_line[h].trim().equals("")) {
	                			
	                			if (check_input_integer_value(tokens_line[h]) || (tokens_line[h].equals(".") && realValue.length()>0)) {
	                				realValue+=tokens_line[h];
	                			}
	                			else if(!realValue.equals("") && !(check_input_integer_value(tokens_line[h]) || tokens_line[h].equals("."))) {
	                				al_token.add(realValue);
	                				realValue="";
	                				al_token.add(tokens_line[h]);
	                			}
	                			else if(realValue.equals("")) {
	                				al_token.add(tokens_line[h]);
	                			}
	                			
	                		}
	                		
	                	}
	                	
	                	String[] tokens = new String[al_token.size()];
	                	al_token.toArray(tokens);

	                    for (int j = 0; j < tokens.length; j++) {
	                        String token = tokens[j].trim();
	                        
	                        if (!token.isEmpty()) {
	                            if(check_input_real_value(token)) {
	                            	al_code.add(token);
                                    al_code_string.add(check_input_type(token));
                                    al_code_no_filter.add(token);
	                            }
	                            else if (reservedWordsList.contains(token) || token.matches("[a-zA-Z0-9|<>]+")) {
	                                if (j < tokens.length - 2 && (token.equals("<") || token.equals("|") || token.equals(":") || token.equals(">")) && String.valueOf(tokens[j + 1]).equals("=")) {
	                                    al_code.add(token + String.valueOf(tokens[j + 1]));
	                                    al_code_string.add(check_input_type(token + String.valueOf(tokens[j + 1])));
	                                    al_code_no_filter.add(token + String.valueOf(tokens[j + 1]));
	                                    j++;
	                                } else {
	                                    al_code.add(token);
	                                    al_code_string.add(check_input_type(token));
	                                    al_code_no_filter.add(token);
	                                }
	                            } else {
	                            	if(token.contains("_") || token.contains("!") || token.contains("@") || token.contains("#") ||
	                            	   token.contains("$") || token.contains("%") || token.contains("^") || token.contains("&") ||
	                            	   token.contains("*") || token.contains("(") || token.contains(")") || token.contains("-") ||
	                            	   token.contains("=") || token.contains("+") || token.contains("{") || token.contains("}") ||
	                            	   token.contains("[") || token.contains("]") || token.contains(".") || token.contains(",") ||
	                            	   token.contains(";") || token.contains(":")) {
	                            		al_code.add(token);
                                        al_code_string.add(check_input_type(token));
                                        al_code_no_filter.add(token);
	                            	}
	                            	else {
	                            		for (char ch : token.toCharArray()) {
		                                    String charStr = String.valueOf(ch);
		                                    if (!charStr.trim().isEmpty()) {
		                                        al_code.add(charStr);
		                                        al_code_string.add(check_input_type(charStr));
		                                        al_code_no_filter.add(charStr);
		                                    }
		                                }
	                            	}
	                            	
	                                
	                            }
	                        }
	                    }
	                }

	                hm_code_line.put(i + 1, al_code);
	                i++;
	            }

	            System.out.println(hm_code_line.toString());
	            System.out.println("---------------------------------------------------------------------");
	            System.out.println("Code tokens: " + al_code_string.toString());
	            System.out.println("\n\n\n");
	            in.close();
	        } catch (Exception e) {
	            e.getMessage();
	            e.printStackTrace();
	        }
	    }
	}

	
	
	// check input type
	public static String check_input_type(String s) {	
		
		// check if it reserve words
		for(int i=0;i<arr_reserved_word.length;i++) {
			if(s.equals(arr_reserved_word[i])) {
				return arr_reserved_word[i];
			}
		}
		
		// check if it operators
		for(int i=0;i<arr_operator.length;i++) {
			if(s.equals(arr_operator[i])) {
				return arr_operator[i];
			}
		}
		
		// check if it name
		if(check_input_name(s)) {
			return "name";
		}
		// check if it integer value
		else if(check_input_integer_value(s)) {
			return "integer-value";
		}
		// check if it real value
		else if(check_input_real_value(s)) {
			return "real-value";
		}
		
		
		
		return null;
	}
	
	// check if the input name
	public static boolean check_input_name(String s) {
        Pattern p=Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
        Matcher m=p.matcher(s);
        return m.matches();
    }
	
	// check if the input integer value
	public static boolean check_input_integer_value(String s) {
        Pattern p=Pattern.compile("\\d+");
        Matcher m=p.matcher(s);
        return m.matches();
    }
	
	// check if the input real value
	public static boolean check_input_real_value(String s) {
        Pattern p=Pattern.compile("\\d+(\\.\\d+)?");
        Matcher m=p.matcher(s);
        return m.matches();
    }
	
	
	
	// check Syntax of code
	public static void check_code_syntax() {
		
		// add starting symbol to the stack
		stack.add(al_non_terminals.get(0));
		
		System.out.println("=====================================================================");
		System.out.println("The syntax check: "+"\n\n");
		
		
		while(!stack.isEmpty() || !al_code_string.isEmpty()) {
			
			// error
			if(al_code_string.get(0)==null) {
				System.out.println("Error in code !!!");
				int temp=0;
				for(int i=0;i<hm_code_line.size();i++) {
					if(temp+hm_code_line.get(i+1).size()<(al_code_no_filter.size()-al_code_string.size()+1)) {
						temp+=hm_code_line.get(i+1).size();
						num_line_error++;
					}
					else{
						break;
					}
				}
				
				if(stack.peek().equals(";")) {
					string_error=";";
					num_line_error--;
					while(hm_code_line.get(num_line_error).isEmpty()) {
						num_line_error--;
					}
					System.out.println("Line ("+num_line_error+"): "+hm_code_line.get(num_line_error)+" the error in missing "+string_error);
		
				}
				else {
					string_error=al_code_no_filter.get(al_code_no_filter.size()-al_code_string.size());
					System.out.println("Line ("+num_line_error+"): "+hm_code_line.get(num_line_error)+" the error in "+string_error);
				
				}
				break;
			}
			else {
				System.out.println("stack: "+stack.toString());
				System.out.println("al: "+al_code_string.get(0).toString());
				System.out.println("-----------------------------------------------");
				System.out.println("\n\n");
				
				
				if(stack.peek().equals(al_code_string.get(0))) {
					stack.pop();
					al_code_string.remove(0);
				}
				else if(stack.peek().equals("lambda")) {
					stack.pop();
				}
				else {
					
					String production_number="";
					
					// error
					if((al_non_terminals.equals(stack.peek()) && al_non_terminals.equals(al_code_string.get(0))) || (al_terminals.equals(stack.peek()) && al_terminals.equals(al_code_string.get(0)))) {
						System.out.println("Error in code !!!");
						int temp=0;
						for(int i=0;i<hm_code_line.size();i++) {
							if(temp+hm_code_line.get(i+1).size()<(al_code_no_filter.size()-al_code_string.size()+1)) {
								num_line_error++;
								temp+=hm_code_line.get(i+1).size();
							}
							else{
								break;
							}
						}
						if(stack.peek().equals(";")) {
							string_error=";";
							num_line_error--;
							while(hm_code_line.get(num_line_error).isEmpty()) {
								num_line_error--;
							}
							System.out.println("Line ("+num_line_error+"): "+hm_code_line.get(num_line_error)+" the error in missing "+string_error);
					
						}
						else {
							string_error=al_code_no_filter.get(al_code_no_filter.size()-al_code_string.size());
							System.out.println("Line ("+num_line_error+"): "+hm_code_line.get(num_line_error)+" the error in "+string_error);
							
						}
						
						break;
					}
					// search the number of production where take peek of stack with the first string from code tokens
					for(int i=0;i<ll1_parse_table.length;i++) {
						for(int j=0;j<ll1_parse_table[i].length;j++) {
							if(ll1_parse_table[i][0].equals(stack.peek()) && ll1_parse_table[0][j].equals(al_code_string.get(0))) {
								production_number=ll1_parse_table[i][j];
								break;
							}
						}
					}
					
					// error
					if(production_number.equals("") || production_number.equals(" ")) {
						System.out.println("Error in code !!!");
						int temp=0;
						for(int i=0;i<hm_code_line.size();i++) {
							if(temp+hm_code_line.get(i+1).size()<(al_code_no_filter.size()-al_code_string.size()+1)) {
								num_line_error++;
								temp+=hm_code_line.get(i+1).size();
							}
							else{
								break;
							}
						}
						if(stack.peek().equals(";")) {
							string_error=";";
							num_line_error--;
							while(hm_code_line.get(num_line_error).isEmpty()) {
								num_line_error--;
							}
							System.out.println("Line ("+num_line_error+"): "+hm_code_line.get(num_line_error)+" the error in missing "+string_error);
						
						}
						else if(stack.peek().equals("more-names") && al_code_string.get(0).equals("name")) {
							string_error=al_code_no_filter.get(al_code_no_filter.size()-al_code_string.size()-1);
							System.out.println("Line ("+(num_line_error-1)+"): "+hm_code_line.get(num_line_error-1)+" the error in "+string_error);
						}
						else {
							string_error=al_code_no_filter.get(al_code_no_filter.size()-al_code_string.size());
							System.out.println("Line ("+num_line_error+"): "+hm_code_line.get(num_line_error)+" the error in "+string_error);
							
						}
						break;
					}
					else {
						ArrayList<String> al_res=hm_production_number.get(Integer.parseInt(production_number));
						System.out.println("Action: production ("+production_number+")"+" al:"+al_res.toString());
						stack.pop();
						
						for(int i=al_res.size()-1;i>=0;i--) {
							stack.push(al_res.get(i));
						}
					}
					
				}
				
				
			}
			
		}
		
		if(stack.isEmpty() && al_code_string.isEmpty()) {
			System.out.println("The code is correct");
			
		}
		
		
	}

	
	

	
	
	
	

}
