import java.io.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SampleFileReader {
	
	HashMap<String, String> documentsMap = new HashMap<String, String>();
	List<String> stopWordsList = new ArrayList<String>();
	//total counts of words in all documents combined
	HashMap<String, Integer> totalWordsMap = new HashMap<String, Integer>();
	
	//each word with the frequency of each document as value
	HashMap<String, String> wordFrequencyMap = new HashMap<String, String>();

	public static void main(String[] args) {
		
		SampleFileReader reader = new SampleFileReader();
		long startTime = System.currentTimeMillis();
		reader.splitInputDocument();
		reader.readDocuments();
		reader.writeWordCountToFile();
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total time taken(in ms) : "+(endTime-startTime));
	}
	
	/*
	 * This method is to split the whole input doucment and build individual sub documents
	 */
	public void splitInputDocument(){
		try {
			
			List<String> totalLines = Files.readAllLines(Paths.get("C:\\Users\\santo\\.jenkins\\jobs\\SampleFileReader\\workspace\\cran.all.1400"), StandardCharsets.UTF_8);
			System.out.println("Total lines in file ==>"+totalLines.size());
			
			stopWordsList = Files.readAllLines(Paths.get("C:\\Users\\santo\\.jenkins\\jobs\\SampleFileReader\\workspace\\stopwords.txt"), StandardCharsets.UTF_8);
			System.out.println("total stop words count ==>"+stopWordsList.size());
			
			
			String eachLine = null;
			String documentName = null;
			boolean blnBocumentText = false;
			StringBuffer documentText = new StringBuffer();
			for(int i=0; i< totalLines.size();i++){
				eachLine = totalLines.get(i);
				//document name starts with T
				if(eachLine.startsWith(".I")){
					if(blnBocumentText) {
						documentsMap.put(documentName, documentText.toString());
						blnBocumentText = false;
						documentText = new StringBuffer();
					}
					
					documentName = eachLine.substring(2).trim();
				}
				else if(eachLine.startsWith(".W")){//Document text starts after tag .W
					blnBocumentText = true;
				}
				else {
					if(blnBocumentText){
						if(documentText.length()>0){
							documentText.append(" ");
							documentText.append(eachLine.trim());
						}
						else {
							documentText.append(eachLine.trim());
						}
					}
					
				}
				
				//to insert last document
				if(i==totalLines.size()-1){
					documentsMap.put(documentName, documentText.toString());
				}
				
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Total Number of Documents ==>"+documentsMap.size());
		
	}
	
	/*
	 * This method reads the individual sub documents and build maps that contain total count of each word and another map to hold the frequency
	 * information of each word per document
	 */
	public void readDocuments(){
		int totalWords = 0;
		String word = null;
		try{
			Iterator docIterator = documentsMap.keySet().iterator();
		    while (docIterator.hasNext()) {
		        String docName = (String)docIterator.next();
		        String documentText = documentsMap.get(docName);
		      //documentText = documentText.replaceAll("[^a-zA-Z0-9]", "");//if numbers should not be skipped
		        documentText = documentText.replaceAll("[^a-zA-Z]", " ");//if numbers should be skipped
		        
		        String []docWords=documentText.split(" ");
		        
				int wordCount= 0;
				HashMap<String, Integer> eachDocWordCollection = new HashMap<String, Integer>();
		        for(int i=0; i<docWords.length;i++) {
		        	word = docWords[i].trim();
		        	
		        	if(word.length()==0)
	            		 continue;
	            	 
		             if(!stopWordsList.contains(word)){
		            	 totalWords++;
		            	Stemmer stemmerObject = new Stemmer();
				        stemmerObject.add(word.toCharArray(), word.length());
				        stemmerObject.stem();
			        	word = stemmerObject.toString();
			        	
				        	
		            	 //to get word freq in this document
		            	 if(eachDocWordCollection.containsKey(word)) {
		            		 int count = eachDocWordCollection.get(word);
		            		 eachDocWordCollection.put(word, count+1);
		            	 }
		            	 else{
		            		 eachDocWordCollection.put(word, 1);
		            	 }
		            	 
		            	 //to collect total word count over all documents
		            	 if(totalWordsMap.containsKey(word)) {
		            		 int count = totalWordsMap.get(word);
		            		 totalWordsMap.put(word, count+1);
		            	 }
		            	 else{
		            		 totalWordsMap.put(word, 1);
		            	 }
		             }
		        }
		        
		        Iterator wordMapIterator = eachDocWordCollection.keySet().iterator();
		        String frequencyValue = null;
			    while (wordMapIterator.hasNext()) {
			    	word = (String)wordMapIterator.next();
			    	wordCount = eachDocWordCollection.get(word);
			    	
			    	if(wordFrequencyMap.containsKey(word)){
			    		frequencyValue = wordFrequencyMap.get(word);
			    		wordFrequencyMap.put(word,frequencyValue+"|"+"docName:"+docName+ "|"+"frequency:"+wordCount);
			    	}
			    	else{
			    		wordFrequencyMap.put(word,"docName:"+docName+ "|"+"frequency:"+wordCount);
			    	}
			    	
			    }
		        
		        
		    }
		    
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		System.out.println("Total Words (excluding stop words and after stemmer is applied) : "+totalWords);
		System.out.println("Total unique Words (excluding stop words and after stemmer is applied) : "+totalWordsMap.size());
		
		//to print the postings of each word, shortest posting word and longest posting word and respective lengths
		Iterator wordMapIterator = wordFrequencyMap.keySet().iterator();
        String value = null;
        String shortestpostingWord = null;
        String longpostingWord = null;
        int shortestposting= 0;
        int longposting = 0;
	    while (wordMapIterator.hasNext()) {
	    	word = (String)wordMapIterator.next();
	    	value = wordFrequencyMap.get(word);
	    	int count = 0;
	        int idx = 0;

	        while ((idx = value.indexOf("docName", idx)) != -1)
	        {
	           idx++;
	           count++;
	        }
	        if(shortestposting ==0){
	        	shortestposting=count;
	        	shortestpostingWord = word;
	        }
	        else if(count < shortestposting){
	        	shortestposting=count;
	        	shortestpostingWord = word;
	        }
	        	
	        
	        if(longposting < count){
	        	longposting=count;
	        	longpostingWord = word;
	        }
	        	
	        
	        //System.out.println("Postings for word '"+word+"' is "+count);
	    }
	    
	    System.out.println("shortest posting word is '"+shortestpostingWord+"' and posting length is "+shortestposting);
	    System.out.println("longest posting word is '"+longpostingWord+"' and posting length is "+longposting);
	}
	
	//Write the output to a file
	public void writeWordCountToFile(){
		
		String word = null;
		int count= 0;
		File outputFile=new File("C:\\Users\\santo\\.jenkins\\jobs\\SampleFileReader\\workspace\\output.txt");
		FileOutputStream fos=null;
		 PrintWriter wordWriter=null;
		try{
		
	    fos=new FileOutputStream(outputFile);
	    wordWriter=new PrintWriter(fos);
	    
	        Iterator wordIterator = totalWordsMap.keySet().iterator();
		    while (wordIterator.hasNext()) {
		    	word = (String)wordIterator.next();
		        count = totalWordsMap.get(word);
		        wordWriter.println(word.trim()+"|Total Frequency:"+count +"|"+wordFrequencyMap.get(word));
		    	
		    }

	        
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		finally{
			wordWriter.flush();
			wordWriter.close();
			try{
				fos.close();
			}
	        catch(Exception ex){}
			
			double filesize = outputFile.length()/1024;
			System.out.println("Size of the output file (in KB) : "+filesize);
			
		}
	}
	
	/**
	  * Stemmer, implementing the Porter Stemming Algorithm
	  *
	  * The Stemmer class transforms a word into its root form.  The input
	  * word can be provided a character at time (by calling add()), or at once
	  * by calling one of the various stem(something) methods.
	  */

	class Stemmer
	{  private char[] b;
	   private int i,     /* offset into b */
	               i_end, /* offset to end of stemmed word */
	               j, k;
	   private static final int INC = 50;
	                     /* unit of size whereby b is increased */
	   public Stemmer()
	   {  b = new char[INC];
	      i = 0;
	      i_end = 0;
	   }

	   /**
	    * Add a character to the word being stemmed.  When you are finished
	    * adding characters, you can call stem(void) to stem the word.
	    */

	   public void add(char ch)
	   {  if (i == b.length)
	      {  char[] new_b = new char[i+INC];
	         for (int c = 0; c < i; c++) new_b[c] = b[c];
	         b = new_b;
	      }
	      b[i++] = ch;
	   }


	   /** Adds wLen characters to the word being stemmed contained in a portion
	    * of a char[] array. This is like repeated calls of add(char ch), but
	    * faster.
	    */

	   public void add(char[] w, int wLen)
	   {  if (i+wLen >= b.length)
	      {  char[] new_b = new char[i+wLen+INC];
	         for (int c = 0; c < i; c++) new_b[c] = b[c];
	         b = new_b;
	      }
	      for (int c = 0; c < wLen; c++) b[i++] = w[c];
	   }

	   /**
	    * After a word has been stemmed, it can be retrieved by toString(),
	    * or a reference to the internal buffer can be retrieved by getResultBuffer
	    * and getResultLength (which is generally more efficient.)
	    */
	   public String toString() { return new String(b,0,i_end); }

	   /**
	    * Returns the length of the word resulting from the stemming process.
	    */
	   public int getResultLength() { return i_end; }

	   /**
	    * Returns a reference to a character buffer containing the results of
	    * the stemming process.  You also need to consult getResultLength()
	    * to determine the length of the result.
	    */
	   public char[] getResultBuffer() { return b; }

	   /* cons(i) is true <=> b[i] is a consonant. */

	   private final boolean cons(int i)
	   {  switch (b[i])
	      {  case 'a': case 'e': case 'i': case 'o': case 'u': return false;
	         case 'y': return (i==0) ? true : !cons(i-1);
	         default: return true;
	      }
	   }

	   /* m() measures the number of consonant sequences between 0 and j. if c is
	      a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
	      presence,

	         <c><v>       gives 0
	         <c>vc<v>     gives 1
	         <c>vcvc<v>   gives 2
	         <c>vcvcvc<v> gives 3
	         ....
	   */

	   private final int m()
	   {  int n = 0;
	      int i = 0;
	      while(true)
	      {  if (i > j) return n;
	         if (! cons(i)) break; i++;
	      }
	      i++;
	      while(true)
	      {  while(true)
	         {  if (i > j) return n;
	               if (cons(i)) break;
	               i++;
	         }
	         i++;
	         n++;
	         while(true)
	         {  if (i > j) return n;
	            if (! cons(i)) break;
	            i++;
	         }
	         i++;
	       }
	   }

	   /* vowelinstem() is true <=> 0,...j contains a vowel */

	   private final boolean vowelinstem()
	   {  int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
	      return false;
	   }

	   /* doublec(j) is true <=> j,(j-1) contain a double consonant. */

	   private final boolean doublec(int j)
	   {  if (j < 1) return false;
	      if (b[j] != b[j-1]) return false;
	      return cons(j);
	   }

	   /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
	      and also if the second c is not w,x or y. this is used when trying to
	      restore an e at the end of a short word. e.g.

	         cav(e), lov(e), hop(e), crim(e), but
	         snow, box, tray.

	   */

	   private final boolean cvc(int i)
	   {  if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
	      {  int ch = b[i];
	         if (ch == 'w' || ch == 'x' || ch == 'y') return false;
	      }
	      return true;
	   }

	   private final boolean ends(String s)
	   {  int l = s.length();
	      int o = k-l+1;
	      if (o < 0) return false;
	      for (int i = 0; i < l; i++) if (b[o+i] != s.charAt(i)) return false;
	      j = k-l;
	      return true;
	   }

	   /* setto(s) sets (j+1),...k to the characters in the string s, readjusting
	      k. */

	   private final void setto(String s)
	   {  int l = s.length();
	      int o = j+1;
	      for (int i = 0; i < l; i++) b[o+i] = s.charAt(i);
	      k = j+l;
	   }

	   /* r(s) is used further down. */

	   private final void r(String s) { if (m() > 0) setto(s); }

	   /* step1() gets rid of plurals and -ed or -ing. e.g.

	          caresses  ->  caress
	          ponies    ->  poni
	          ties      ->  ti
	          caress    ->  caress
	          cats      ->  cat

	          feed      ->  feed
	          agreed    ->  agree
	          disabled  ->  disable

	          matting   ->  mat
	          mating    ->  mate
	          meeting   ->  meet
	          milling   ->  mill
	          messing   ->  mess

	          meetings  ->  meet

	   */

	   private final void step1()
	   {  if (b[k] == 's')
	      {  if (ends("sses")) k -= 2; else
	         if (ends("ies")) setto("i"); else
	         if (b[k-1] != 's') k--;
	      }
	      if (ends("eed")) { if (m() > 0) k--; } else
	      if ((ends("ed") || ends("ing")) && vowelinstem())
	      {  k = j;
	         if (ends("at")) setto("ate"); else
	         if (ends("bl")) setto("ble"); else
	         if (ends("iz")) setto("ize"); else
	         if (doublec(k))
	         {  k--;
	            {  int ch = b[k];
	               if (ch == 'l' || ch == 's' || ch == 'z') k++;
	            }
	         }
	         else if (m() == 1 && cvc(k)) setto("e");
	     }
	   }

	   /* step2() turns terminal y to i when there is another vowel in the stem. */

	   private final void step2() { if (ends("y") && vowelinstem()) b[k] = 'i'; }

	   /* step3() maps double suffices to single ones. so -ization ( = -ize plus
	      -ation) maps to -ize etc. note that the string before the suffix must give
	      m() > 0. */

	   private final void step3() { if (k == 0) return; /* For Bug 1 */ switch (b[k-1])
	   {
	       case 'a': if (ends("ational")) { r("ate"); break; }
	                 if (ends("tional")) { r("tion"); break; }
	                 break;
	       case 'c': if (ends("enci")) { r("ence"); break; }
	                 if (ends("anci")) { r("ance"); break; }
	                 break;
	       case 'e': if (ends("izer")) { r("ize"); break; }
	                 break;
	       case 'l': if (ends("bli")) { r("ble"); break; }
	                 if (ends("alli")) { r("al"); break; }
	                 if (ends("entli")) { r("ent"); break; }
	                 if (ends("eli")) { r("e"); break; }
	                 if (ends("ousli")) { r("ous"); break; }
	                 break;
	       case 'o': if (ends("ization")) { r("ize"); break; }
	                 if (ends("ation")) { r("ate"); break; }
	                 if (ends("ator")) { r("ate"); break; }
	                 break;
	       case 's': if (ends("alism")) { r("al"); break; }
	                 if (ends("iveness")) { r("ive"); break; }
	                 if (ends("fulness")) { r("ful"); break; }
	                 if (ends("ousness")) { r("ous"); break; }
	                 break;
	       case 't': if (ends("aliti")) { r("al"); break; }
	                 if (ends("iviti")) { r("ive"); break; }
	                 if (ends("biliti")) { r("ble"); break; }
	                 break;
	       case 'g': if (ends("logi")) { r("log"); break; }
	   } }

	   /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

	   private final void step4() { switch (b[k])
	   {
	       case 'e': if (ends("icate")) { r("ic"); break; }
	                 if (ends("ative")) { r(""); break; }
	                 if (ends("alize")) { r("al"); break; }
	                 break;
	       case 'i': if (ends("iciti")) { r("ic"); break; }
	                 break;
	       case 'l': if (ends("ical")) { r("ic"); break; }
	                 if (ends("ful")) { r(""); break; }
	                 break;
	       case 's': if (ends("ness")) { r(""); break; }
	                 break;
	   } }

	   /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

	   private final void step5()
	   {   if (k == 0) return; /* for Bug 1 */ switch (b[k-1])
	       {  case 'a': if (ends("al")) break; return;
	          case 'c': if (ends("ance")) break;
	                    if (ends("ence")) break; return;
	          case 'e': if (ends("er")) break; return;
	          case 'i': if (ends("ic")) break; return;
	          case 'l': if (ends("able")) break;
	                    if (ends("ible")) break; return;
	          case 'n': if (ends("ant")) break;
	                    if (ends("ement")) break;
	                    if (ends("ment")) break;
	                    /* element etc. not stripped before the m */
	                    if (ends("ent")) break; return;
	          case 'o': if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) break;
	                                    /* j >= 0 fixes Bug 2 */
	                    if (ends("ou")) break; return;
	                    /* takes care of -ous */
	          case 's': if (ends("ism")) break; return;
	          case 't': if (ends("ate")) break;
	                    if (ends("iti")) break; return;
	          case 'u': if (ends("ous")) break; return;
	          case 'v': if (ends("ive")) break; return;
	          case 'z': if (ends("ize")) break; return;
	          default: return;
	       }
	       if (m() > 1) k = j;
	   }

	   /* step6() removes a final -e if m() > 1. */

	   private final void step6()
	   {  j = k;
	      if (b[k] == 'e')
	      {  int a = m();
	         if (a > 1 || a == 1 && !cvc(k-1)) k--;
	      }
	      if (b[k] == 'l' && doublec(k) && m() > 1) k--;
	   }

	   /** Stem the word placed into the Stemmer buffer through calls to add().
	    * Returns true if the stemming process resulted in a word different
	    * from the input.  You can retrieve the result with
	    * getResultLength()/getResultBuffer() or toString().
	    */
	   public void stem()
	   {  k = i - 1;
	      if (k > 1) { step1(); step2(); step3(); step4(); step5(); step6(); }
	      i_end = k+1; i = 0;
	   }

	}

}

