import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

public class supervisedLearning {
	//create nonTerms array
	static String [] nonTerms;
	//create the chart, the grammar array, wordType array, words array, and words probability array
	static Tree [][][] myChart;
	static double [][][] grammar;
	static String[] wordType;
	static String[] words;
	static double [][] wordsProb;
	
	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		//set up
		if(args.length<2) {
			System.out.println("Please follow the readme prompt.");
			return;
		}
		int trainingNum=Integer.parseInt(args[1]);
		File f=new File(args[0]);
		Scanner in=new Scanner(f);
		boolean summary=false;
		if(args.length>2) {
			if(args[2].equals("s")) {
				summary=true;
			}
		}
		//store the trees of training set
		ArrayList<Node> trees=new ArrayList<Node>();
		//store the count of different nonTerms and grammar and lexicon
		Hashtable<String,Integer> hashtable=new Hashtable<String,Integer>();
		//mark the word to be iterated by putting into an array
		ArrayList<String> iterated=new ArrayList<String>();
		//count the nonterms, wordtypes, and words
		int countForNonTerms=0;
		int countForWordTypes=0;
		int countForWords=0;
		//store the training set into arraylist
		
		ArrayList<String> trainingSet=new ArrayList<String>();
		for(int j=0;j<trainingNum;j++) {
			String curLine=in.nextLine();
			
			String[] temp=curLine.split(" ");
			int count=0;
			for(int i=0;i<temp.length;i++) {
				if(temp[i].equals("")) {
					
				}else {
					count++;
					if(temp[i].charAt(0)=='*' && !iterated.contains(temp[i])) {
						iterated.add(temp[i]);
						countForNonTerms++;
					}else if(temp[i].charAt(0)=='+' && !iterated.contains(temp[i])) {
						iterated.add(temp[i]);
						countForWordTypes++;
						countForNonTerms++;
					}else if(!iterated.contains(temp[i])) {
						iterated.add(temp[i]);
						countForWords++;
					}
				}
			}
			String [] temp2=new String[count];
			int index=0;
			for(int i=0;i<temp.length;i++) {
				if(temp[i].equals("")) {
					
				}else {
					temp2[index]=temp[i];
					index++;
				}
			}
			String temp3="";
			for(int i=0;i<temp2.length;i++) {
				temp3+=temp2[i];
				if(temp2.length!=i+1) {
					temp3+=" ";
				}
			}
			trainingSet.add(temp3);
			//make the expression a tree of nodes
			NK b=prefixToExpTree(temp2,0);
			Node c=b.N;
			trees.add(c);
			//count the grammar and lexicon and nonterms
			inorder(c,hashtable);
		}
		//create arrays for nonterms, wordtype and words
		nonTerms=new String[countForNonTerms];
		wordType=new String[countForWordTypes];
		words=new String[countForWords];
		//fill the arrays
		for(int i=0;i<trainingSet.size();i++) {
			String[] curLine=trainingSet.get(i).split(" ");
			for(int j=0;j<curLine.length;j++) {
				if(curLine[j].charAt(0)=='*' && indexOf(curLine[j],nonTerms)==-1) {
					fillArray(curLine[j],nonTerms);
				}else if(curLine[j].charAt(0)=='+' && indexOf(curLine[j],wordType)==-1) {
					fillArray(curLine[j],wordType);
					fillArray(curLine[j],nonTerms);
				}else if(curLine[j].charAt(0)!='+'&&curLine[j].charAt(0)!='*'&&indexOf(curLine[j],words)==-1) {
					fillArray(curLine[j],words);
				}
			}
		}
		//create tables for wordprob and grammar
		wordsProb=new double[wordType.length][words.length];
		grammar=new double[nonTerms.length][nonTerms.length][nonTerms.length];
		

		//print out the grammar
		System.out.println("Grammar");
		//this store the covered grammar
		ArrayList<String> coveredGrammar=new ArrayList<String>();
		Set<String> keys=hashtable.keySet();
		
		pgrammar("*S",keys,hashtable,coveredGrammar,grammar);
		
		System.out.println();
		
		//print out lexicon
		System.out.println("Lexicon");
		//this store the coveredLexicon
		ArrayList<String> coveredLexicon=new ArrayList<String>();
		plexicon(keys,hashtable,coveredLexicon,wordsProb);
		//store the testing set
		ArrayList<String> testingSet=new ArrayList<String>();
		while(in.hasNextLine()) {
			String line="";
			String [] linesplit=in.nextLine().split(" ");
			for(int i=0;i<linesplit.length;i++) {
				if(!linesplit[i].equals("")) {
					line=line+linesplit[i]+" ";
				}
			}
			testingSet.add(line.trim());
		}
		//sentences are the words without grammar and lexicon
		ArrayList<String> sentences=new ArrayList<String>();
		for(int i=0;i<testingSet.size();i++) {
			String []curLine=testingSet.get(i).split(" ");
			String temp="";
			for(int j=0;j<curLine.length;j++) {
				if(!curLine[j].equals("") &&curLine[j].charAt(0)!='*'&&curLine[j].charAt(0)!='+') {
					temp=temp+curLine[j]+" ";
				}
			}
			temp=temp.substring(0,temp.length()-1);
			
			sentences.add(temp);
		}
		//count the correct ones
		int countForCorrect=0;
		//print out the parses if summary flag is not set
		if(!summary) {
			System.out.println();
			System.out.println("Parses:");
		}
		for(int i=0;i<sentences.size();i++) {
			String curSen=sentences.get(i);
			String curResult=cyk_parse(curSen);
			if(curResult!=null) {
				if(curResult.equals(testingSet.get(i).trim())) {
					if(!summary) {
						System.out.println(curResult+" Right");
					}
					countForCorrect++;
				}else {
					if(!summary) {
						System.out.println(curResult+" Wrong");
					}
				}
			}else {
				if(!summary) {
					System.out.println("This sentence cannot be parsed. Wrong");
				}
			}
		}
		double accuracy=(countForCorrect+0.0)/sentences.size();
		DecimalFormat numberFormat = new DecimalFormat("0.0###");
		String aaa=numberFormat.format(accuracy);
		//print out the accuracy
		System.out.println();
		System.out.println(String.format("Accuracy: The parser was tested on %d sentences. It got %d right, for an accuracy of %s", sentences.size(),countForCorrect,aaa));

		in.close();
	}
	
	//this function fills an array
	static void fillArray(String str, String[] array) {
		for(int i=0;i<array.length;i++) {
			if(array[i]==null) {
				array[i]=str;
				break;
			}
		}
	}
	
	//this function prints the grammar
	static void pgrammar(String str,Set<String> keys,Hashtable<String,Integer> hashtable, ArrayList<String>coveredGrammar,double[][][] grammar) {
		ArrayList<String> nextWork=new ArrayList<String>();
		for(String key: keys) {
			String[] temp=key.split(" ");
			if(temp[0].contains(str)) {
				if(!coveredGrammar.contains(temp[0])) {
					coveredGrammar.add(temp[0]);
				}
				if(temp.length>2) {
					String s1=temp[0].replaceAll("\\*", "").replaceAll("\\+", "");
					String s2=temp[1].replaceAll("\\*", "").replaceAll("\\+", "");
					String s3=temp[2].replaceAll("\\*", "").replaceAll("\\+", "");
					DecimalFormat numberFormat = new DecimalFormat("0.0###");
					double d1=(hashtable.get(key)+0.0)/hashtable.get(str);
					System.out.println(String.format("%s -> %s %s [%s]", s1,s2,s3,numberFormat.format(d1)));

					grammar[indexOf(temp[0],nonTerms)][indexOf(temp[1],nonTerms)][indexOf(temp[2],nonTerms)]=d1;
					if(temp[1].charAt(0)=='*' && !coveredGrammar.contains(temp[1]) && !nextWork.contains(temp[1])) {
						nextWork.add(temp[1]);
					}
					if(temp[2].charAt(0)=='*' && !coveredGrammar.contains(temp[2]) && !nextWork.contains(temp[2])) {
						nextWork.add(temp[2]);
					}
				}
			}
		}
		for(int i=0;i<nextWork.size();i++) {
			String next=nextWork.remove(0);
			pgrammar(next,keys,hashtable,coveredGrammar,grammar);
		}
		
	}
	
	//this function prints the lexicon
	static void plexicon(Set<String> keys,Hashtable<String,Integer> hashtable, ArrayList<String>coveredLexicon,double[][]wordsProb) {
		DecimalFormat numberFormat = new DecimalFormat("0.0###");
		Hashtable<String,String> results=new Hashtable<String,String>();
		for(String key: keys) {
			String[] temp=key.split(" ");
			if(!coveredLexicon.contains(temp[0])&&temp[0].charAt(0)=='+') {
				coveredLexicon.add(temp[0]);
				results.put(temp[0], "");
			}
			if(temp[0].charAt(0)=='+'&&temp.length==2) {
				String s1=temp[0].replaceAll("\\*", "").replaceAll("\\+", "");
				String s2=temp[1].replaceAll("\\*", "").replaceAll("\\+", "");
				double d1=(hashtable.get(key)+0.0)/hashtable.get(temp[0]);
				String s3=String.format("%s -> %s [%s]\n", s1,s2,numberFormat.format(d1));
				results.put(temp[0], results.get(temp[0])+s3);
				wordsProb[indexOf(temp[0],wordType)][indexOf(temp[1],words)]=d1;
			}
		}
		Set<String> keysOfResults=results.keySet();
		for(String key:keysOfResults) {
			System.out.print(results.get(key));
		}
	}


	//this function return the tree in prefix format
	static String printTree1(Tree tree){
		
		if(tree!=null) {
			String s=tree.phrase+" ";
			if(tree.word!=null) {
				s=s+tree.word+" ";
			}
			
			return s+printTree1(tree.left)+printTree1(tree.right);
		}else {
			return "";
		}
	}
	//this function make an expression into expression tree
	static NK prefixToExpTree(String [] S, int K) {
		   Node N = new Node();
		   N.value = S[K];
		   K++;
		   if (S[K-1].charAt(0)=='*') {
		     for (int i=0; i < 2; i++) {
		         NK a = prefixToExpTree(S,K);
		         N.array.add(a.N);
		         K=a.K;
		      }
		   }else if(S[K-1].charAt(0)=='+') {
			   NK a = prefixToExpTree(S,K);
		       N.array.add(a.N);
		       K=a.K;
		   }
		   return new NK(N,K);
	}
	
	//an inorder iteration to count the grammar and lexicon
	static void inorder(Node node,Hashtable<String,Integer> h) {
		if(!h.containsKey(node.value)) {
			h.put(node.value, 0);
		}
		h.put(node.value, h.get(node.value)+1);
		if(node.array.size()!=0) {
			String temp=node.value;
			for(int i=0;i<node.array.size();i++) {
				temp+=" ";
				temp+=node.array.get(i).value;
			}
			if(!h.containsKey(temp)) {
				h.put(temp, 0);
			}
			h.put(temp, h.get(temp)+1);
		}
		for(int i=0;i<node.array.size();i++) {
			inorder(node.array.get(i),h);
		}
	}

	
	//this is a function that show the index of a string in an array
	
	static int indexOf(String a, String[]b) {
		int index=-1;
		for(int i=0;i<b.length;i++) {
			if (a.equals(b[i])) {
				index=i;
				break;
			}
		}
		return index;
	}
	
	//this is the cyk parse function
	static String cyk_parse(String sentence) {
		//create the chart with the sentence given
		String [] sen=sentence.toLowerCase().split(" ");
		int n=sen.length;
		myChart=new Tree[nonTerms.length][n][n];
		//initialize the chart with all probability 0 nodes without word
		for(int i=0;i<nonTerms.length;i++) {
			for(int j=0;j<n;j++) {
				for(int k=0;k<n;k++) {
					myChart[i][j][k]=new Tree(nonTerms[i], j,k,null, null, null,0,null,null,0);
				}
			}
		}
		
		//initialize the chart when a word is given
		for(int i=0;i<n;i++) {
			String word=sen[i];
			int index1=indexOf(word,words);
			if(index1==-1) {
				return null;
			}
			for(int j=0;j<wordsProb.length;j++) {
					double p=wordsProb[j][index1];
					myChart[indexOf(wordType[j],nonTerms)][i][i]=new Tree(wordType[j], i,i,word, null, null,p,null,null,0);
			}
		}
		
		//length is length of phrase
		for(int length=2;length<=n;length++) {
			//i is start of phrase
			for(int i=0;i<=n-length;i++) {
				//j is end of phrase
				int j=i+length-1;
				for(int nt=0;nt<nonTerms.length;nt++) {
					//get the phrase with specific non terminal
					myChart[nt][i][j]=new Tree(nonTerms[nt],i,j,null,null,null,0,null,null,0);
					//record the index of this non terminal
					int index2=nt;
					//k is end of first subphrase
					for(int k=i;k<=j-1;k++) {
						for(int it1=0;it1<nonTerms.length;it1++) {
							for(int it2=0;it2<nonTerms.length;it2++) {
								//each phrase separation way and grammar combo are tried
								//newProb is the best probability
								//prob4= max(prob2,prob3) is the second best probability at this node
								double newProb=myChart[it1][i][k].prob*myChart[it2][k+1][j].prob*grammar[index2][it1][it2];
								double prob2=myChart[it1][i][k].secondProb*myChart[it2][k+1][j].prob*grammar[index2][it1][it2];
								double prob3=myChart[it1][i][k].prob*myChart[it2][k+1][j].secondProb*grammar[index2][it1][it2];
								double prob4;
								if(prob2<prob3) {
									prob4=prob3;
								}else {
									prob4=prob2;
								}
								//if prob4 is better than current best, set the prob and secondProb with newProb and prob4
								if(prob4>myChart[index2][i][j].prob) {
									myChart[index2][i][j].left=myChart[it1][i][k];
									myChart[index2][i][j].right=myChart[it2][k+1][j];
									myChart[index2][i][j].prob=newProb;
									myChart[index2][i][j].secondLeft=myChart[it1][i][k];
									myChart[index2][i][j].secondRight=myChart[it2][k+1][j];
									myChart[index2][i][j].secondProb=prob4;
								}else {
									//if prob 4 is not better than current best, we need to look at newProb directly
									//if newProb is better than current best, new best is newProb and new second is old best
									if(newProb>myChart[index2][i][j].prob) {
										myChart[index2][i][j].secondLeft=myChart[index2][i][j].left;
										myChart[index2][i][j].secondRight=myChart[index2][i][j].right;
										myChart[index2][i][j].secondProb=myChart[index2][i][j].prob;
										myChart[index2][i][j].left=myChart[it1][i][k];
										myChart[index2][i][j].right=myChart[it2][k+1][j];
										myChart[index2][i][j].prob=newProb;
									}else if(newProb>myChart[index2][i][j].secondProb) {
										//if newProb is better than current second best, then set it like that
										myChart[index2][i][j].secondLeft=myChart[it1][i][k];
										myChart[index2][i][j].secondRight=myChart[it2][k+1][j];
										myChart[index2][i][j].secondProb=newProb;
									}
								}
							}
						}
					}
				}
			}
		}
		//call print tree function
		return printTree(n);
		
	}
	
	//this function returns a prefix expression of the cyk parse tree
	static String printTree(int len) {
		//if probability is 0 then cannot be parsed
		if(myChart[indexOf("*S",nonTerms)][0][len-1].prob==0) {
			System.out.println("This sentence cannot be parsed.");
			return null;
		}else {
			//print the best tree
			String temp=printTree1(myChart[indexOf("*S",nonTerms)][0][len-1]);
			return temp.substring(0, temp.length()-1);
		}
	}
}
//this is a node of the training set
class Node{
	String value;
	ArrayList<Node> array=new ArrayList<Node>();
	Node(){
		
	}
	Node(String value){
		this.value=value;
	}
	
}
//this data structure combines node and int
class NK{
	Node N;
	int K;
	NK(Node N,int K){
		this.N=N;
		this.K=K;
	}
}
//this is the tree data structure
class Tree{
	String phrase;
	int startPhrase;
	int endPhrase;
	String word;
	Tree left;
	Tree right;
	double prob;
	Tree secondLeft;
	Tree secondRight;
	double secondProb;
	
	//constructor of tree class
	Tree(String phrase, int i,int j,String word, Tree left, Tree right, double prob, Tree sl, Tree sr, double sp){
		this.phrase=phrase;
		startPhrase=i;
		endPhrase=j;
		this.word=word;
		this.left=left;
		this.right=right;
		this.prob=prob;
		this.secondLeft=sl;
		this.secondRight=sr;
		this.secondProb=sp;
	}
}