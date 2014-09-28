import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution{

	public static void main(String[] args){
		Solution obj = new Solution();
		try {
			obj.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	NoteHolder noteHolder = new NoteHolder();
	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));

	public void run() throws IOException {
		long firstTime = System.currentTimeMillis();

		BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\Sina\\Documents\\GitHub\\Evernote-Contest\\src\\input.txt")));
		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/* This list is used to read all lines of the <note> tag and process them all together.
		 * It helps making the process of passing data to the createNote method much easier. */
		Vector<String> lines = null;

		String line = br.readLine();
		while(line != null){
			switch (line) {
			case "CREATE":
				lines = new Vector<String>();
				line = br.readLine();
				while(!line.contains("</note>")){
					lines.add(line);
					line = br.readLine();
				}
				Note newNote = new Note();
				newNote.createNote(lines);
				noteHolder.addNewNote(newNote);
				break;

			case "UPDATE":
				lines = new Vector<String>();
				line = br.readLine();
				while(!line.contains("</note>")){
					lines.add(line);
					line = br.readLine();
				}
				Note updatedNote = new Note();
				updatedNote.createNote(lines);
				noteHolder.updateNote(updatedNote);
				break;

			case "DELETE":
				line = br.readLine();
				noteHolder.delete(line);
				break;

			case "SEARCH":
				line = br.readLine();
				noteHolder.search(line);
				break;

			default:
				bw.write(line + "\n");
				break;
			}

			line = br.readLine();
		}
		br.close();

		bw.write("\n\n\n" + ((System.currentTimeMillis() - firstTime) / 1000.0) + "\n");
		bw.close();
	}

	private class NoteHolder{
		int trashedNotes = 0;
		Vector<Note> notes;
		Map<String, Integer> guidToPositionIndex;
		SortedVector dateToGuidIndex;
		SortedVector tagToGuidIndex;

		public NoteHolder(){
			notes = new Vector<Solution.Note>();
			notes.ensureCapacity(1000000);

			guidToPositionIndex = new LinkedHashMap<String, Integer>();
			dateToGuidIndex = new SortedVector();
			tagToGuidIndex = new SortedVector();
		}

		public Note findNoteByGUID(String guid) {
			int index = guidToPositionIndex.get(guid);
			if (index != -1){
				return notes.get(index);
			}

			return null;
		}

		public void search(String searchTerm) throws IOException{
			Vector<Note> results = null;

			String lowerSearchTerm = searchTerm.toLowerCase();
			String which = "content";

			// Instead of using some cascaded else if structure, I tried to make it more fancier with ? operator! :D
			which = lowerSearchTerm.startsWith("tag:") ? "tag"   : 
				lowerSearchTerm.startsWith("created:") ? "date"  : "content";

			switch (which) {
			case "tag":
				results = tagSearch(searchTerm);
				break;

			case "date":
				results = dateSearch(searchTerm);
				break;

			case "content":
				results = contentSearch(searchTerm);
				break;

			default:
				//System.out.println("Error: " + which);
				break;
			}

			if (results == null || results.size() == 0){
				bw.write("\n");
				return;
			}

			Comparator<Note> compareNotes = new Comparator<Solution.Note>() {

				public int compare(Note o1, Note o2) {
					return o1.getCreated().compareTo(o2.getCreated());
				}
			};

			Collections.sort(results, compareNotes);


			for (int i = 0; i < results.size() - 1; i++){
				bw.write(results.get(i).getGuid() + ",");
			}
			bw.write(results.get(results.size() - 1).getGuid() + "\n");

		}

		private Vector<Note> tagSearch(String searchTerm){
			Vector<Note> results = new Vector<Note>();
			Vector<String> guids = null;

			searchTerm = searchTerm.toLowerCase();
			searchTerm = searchTerm.substring("tag:".length());
			String[] terms = searchTerm.split("\\s");

			for (String term : terms){
				Set<String> localGuids = new HashSet<String>();
				for (int i = 0; i < tagToGuidIndex.size(); i++){
					String tag = tagToGuidIndex.get(i).key;
					if (term.endsWith("*")){
						String tempTerm = term.substring(0, term.length() - 1);
						String regex = String.format(".*^%s.*|.*\\s%s.*", tempTerm, tempTerm);
						Matcher m = (Pattern.compile(regex)).matcher((String) tag);
						if (m.find()){
							localGuids.addAll(tagToGuidIndex.get(i).guids);
						}
					}else{
						String regex = ".*\\b" + term + "\\b.*";
						Matcher m = (Pattern.compile(regex)).matcher((String) tag);
						if (m.find()){
							localGuids.addAll(tagToGuidIndex.get(i).guids);
						}
					}
				}

				if (guids == null){
					guids = new Vector<String>();
					guids.addAll(localGuids);
				}else{
					guids.retainAll(localGuids);
				}

			}

			for (String guid : guids){
				results.add(findNoteByGUID(guid));
			}

			if (results.size() == 0){
				return null;
			}else{
				return results;
			}

		}

		private Vector<Note> dateSearch(String searchTerm){
			Vector<Note> results = new Vector<Note>();
			searchTerm = searchTerm.substring("created:".length());
			for (int i = dateToGuidIndex.size() - 1; i >= 0; i--){
				if ((dateToGuidIndex.get(i).key).compareTo(searchTerm) >= 0){
					Vector<String> guids = dateToGuidIndex.get(i).guids;

					for (String guid : guids){
						results.add(findNoteByGUID(guid));
					}
				}
			}

			if (results.size() == 0){
				return null;
			}else{
				return results;
			}
		}

		private Vector<Note> contentSearch(String searchTerm){
			Vector<Note> results = new Vector<Solution.Note>();
			String[] terms = searchTerm.toLowerCase().split("\\s");

			for (Note note : notes){
				if (note.isTrash()){
					continue;
				}
				boolean hasAll = true;
				String text = note.content.toLowerCase();
				for (String term : terms){
					if (term.endsWith("*")){
						term = term.substring(0, term.length() - 1);
						String regex = String.format(".*^%s.*|.*\\s%s.*", term, term);
						Matcher m = (Pattern.compile(regex)).matcher(text);
						if (!m.find()){
							hasAll = false;
						}
					}else{
						String regex = ".*\\b" + term + "\\b.*";
						Matcher m = (Pattern.compile(regex)).matcher(text);
						if (!m.find()){
							hasAll = false;
						}
					}
				}
				if (hasAll){
					results.add(note);
				}
			}

			return results;
		}

		public void delete(String guid) {
			Note targetNote = findNoteByGUID(guid);

			if (targetNote == null){
				//System.out.println("Error: Note not found to delete.");
				return;
			}

			invalidateNote(targetNote);
		}

		public void updateNote(Note updatedNote) {
			Note oldNote = findNoteByGUID(updatedNote.getGuid());
			if (oldNote == null){
				//System.out.println("Error: Note not found to update.");
				return;
			}

			invalidateNote(oldNote);

			addNewNote(updatedNote);

			if ((double)trashedNotes / notes.size() > 0.5){
				emptyTrashedNotes();
			}

		}

		private void invalidateNote(Note target){
			target.setTrash(true);
			guidToPositionIndex.remove(target.getGuid());

			int tempIndex = dateToGuidIndex.indexOf(new Tuple(dateToString(target.getCreated()), null));
			Vector<String> bucket = null;
			int deletionIndex = -1;
			if (tempIndex >= 0){
				bucket = dateToGuidIndex.get(tempIndex).guids;
				for (int i = 0; i < bucket.size(); i++){
					if (bucket.get(i).equals(target.getGuid())){
						deletionIndex = i;
						break;
					}
				}
				if (deletionIndex >= 0){
					bucket.remove(deletionIndex);
				}
			}

			for (String tag : target.getTags()){
				tempIndex = tagToGuidIndex.indexOf(new Tuple(tag, null));
				if(tempIndex >= 0){
					bucket = tagToGuidIndex.get(tempIndex).guids;
					deletionIndex = -1;
					for (int i = 0; i < bucket.size(); i++){
						if (bucket.get(i).equals(target.getGuid())){
							deletionIndex = i;
							break;
						}
					}
					if (deletionIndex >= 0){
						bucket.remove(deletionIndex);
					}
				}
			}

			trashedNotes++;
		}

		//TODO: I never checked this method to see if it works fine or not.
		private void emptyTrashedNotes(){
			Vector<Note> onlyValidNotes = new Vector<Solution.Note>();

			for (Note note : notes){
				if (!note.isTrash()){
					onlyValidNotes.add(note);
				}
			}

			notes = onlyValidNotes;
			guidToPositionIndex = new HashMap<String, Integer>();

			for (int i = 0; i < onlyValidNotes.size(); i++){
				guidToPositionIndex.put(notes.get(i).getGuid(), (Integer) i);
			}
		}

		public void addNewNote(Note note){
			notes.add(note);

			guidToPositionIndex.put(note.getGuid(), notes.size() - 1);

			int tempIndex = dateToGuidIndex.indexOf(new Tuple(dateToString(note.getCreated()), null));
			Vector<String> bucket = null;
			if (tempIndex < 0){
				bucket = new Vector<String>();
				bucket.add(note.getGuid());
				dateToGuidIndex.insert(new Tuple(dateToString(note.getCreated()), bucket));
			}else{
				bucket = dateToGuidIndex.get(tempIndex).guids;
				bucket.add(note.getGuid());
			}

			for (String tag : note.getTags()){
				tempIndex = tagToGuidIndex.indexOf(new Tuple(tag, null));

				if (tempIndex < 0){
					bucket = new Vector<String>();
					bucket.add(note.getGuid());
					tagToGuidIndex.insert(new Tuple(tag, bucket));
				}else{
					bucket = tagToGuidIndex.get(tempIndex).guids;
					bucket.add(note.getGuid());
				}
			}

		}

		@SuppressWarnings("unused")
		private Date stringToDate(String str){
			DateFormat format = new SimpleDateFormat("yyyyMMdd");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			try {
				return format.parse(str);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}

		private String dateToString(Date date){
			DateFormat format = new SimpleDateFormat("yyyyMMdd");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			return format.format(date);
		}

		@SuppressWarnings("unused")
		public void printAll(){
			for (Note note : noteHolder.notes){
				System.out.println(note);
			}
		}
	}

	private class Note{
		private String guid;
		private Date created;
		private Vector<String> tags = new Vector<String>();
		private String content;

		private boolean trash = false;

		/**
		 * This method should be used to initialize the content of each newly created note.
		 * @param lines This input parameter should contain all the needed raw information as specified in the problem statement
		 */
		public void createNote(Vector<String> lines){
			//Start from 1 to skip the first line. it only contains <note>
			int index = 1;

			// Read and add the GUID to the new note
			setGuid(removeStringTags(lines.get(index).trim(), "guid"));
			index++;

			// Read and add the created date to the new note
			setCreated(removeStringTags(lines.get(index).trim(), "created"));
			index++;

			// Read and add all tags to the new note
			while (lines.get(index).contains("<tag>")){
				addTag(removeStringTags(lines.get(index).trim(), "tag").replace(":", "").toLowerCase());
				index++;
			}

			// Read and add the content to the new note
			if (lines.get(index).trim().contains("<content>")){

				//Skip one line
				index++;

				StringBuilder sb = new StringBuilder();
				while (!lines.get(index).contains("</content>")){
					sb.append(lines.get(index).trim() + "\n");
					index++;
				}

				setContent(sb.toString());
			}
		}

		/**
		 * This method is used to remove the starting and ending tags from each line
		 * @param rawString The line read from the input
		 * @param tag Specific tag used like <created> and etc.
		 * @return String but without the starting and ending tags
		 */
		private String removeStringTags(String rawString, String tag){
			String startTag = "<" + tag + ">";
			String endTag = "</" + tag + ">";
			return rawString.substring(startTag.length(), rawString.length() - endTag.length());
		}

		public String getGuid() {
			return guid;
		}

		public void setGuid(String guid) {
			this.guid = guid;
		}

		/**
		 * This method returns only the java.util.Date object store in the note
		 * @return java.util.Date creation date of the note
		 */
		public Date getCreated() {
			return created;
		}

		/**
		 * This method converts java.util.Date object stored in the note to the ISO 8601 format and 
		 * returns its string value
		 * @return ISO 8601 String value for the creation date of the note
		 */
		public String getCreatedString() {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			return format.format(created).substring(0, 19) + "Z";
		}

		/**
		 * This method is used to convert from an ISO 8601 string date to java.util.Date object to store the note
		 * more easily.
		 * @param created ISO 8601 format date string
		 */
		public void setCreated(String created) {
			created = created.replace("Z", "+00:00");
			created = created.substring(0, 22) + created.substring(23);  
			try {
				this.created = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(created);
			} catch (ParseException e) {
				//System.out.println(e.getStackTrace());
			}
		}

		public Vector<String> getTags() {
			return tags;
		}

		public void addTag(String tag) {
			tags.add(tag);
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("<note>" + "\n");
			sb.append("GUID:\t" + getGuid() + "\n");
			sb.append("Creation date:\t" + getCreated() + "\n");
			sb.append("Another creation date format:\t" + getCreatedString() + "\n");

			sb.append("Tags:\t");
			for (String tag : tags){
				sb.append(tag + "\t");
			}
			sb.append("\n");

			sb.append("\nContent:\n" + getContent());
			sb.append("</note>");

			return sb.toString();
		}

		public boolean isTrash() {
			return trash;
		}

		public void setTrash(boolean trash) {
			this.trash = trash;
		}
	}

	private class SortedVector extends Vector<Tuple>{

		private static final long serialVersionUID = 3871496867211582390L;

		public void insert(Tuple element){
			add(element);
			Collections.sort(this);
		}

		@Override
		public int indexOf(Object o) {
			return binary_search(this, (Tuple) o, 0, size() - 1);
		}

		private int binary_search(Vector<Tuple> array, Tuple key, int start, int end){
			if (size() == 0){
				return -1;
			}
			
			while (end >= start){
				int mid = ((end + start) / 2);

				if(array.get(mid).compareTo(key) == 0){
					return mid;
				}else if (array.get(mid).compareTo(key) < 0){
					start = mid + 1;
				}else {
					end = mid - 1;
				}
			}
			return -1;
		}

	}

	private class Tuple implements Comparable<Tuple>{
		String key;
		Vector<String> guids;

		public Tuple(String key, Vector<String> guids) {
			super();
			this.key = key;
			this.guids = guids;
		}

		@Override
		public int compareTo(Tuple o) {
			return this.key.compareTo(o.key);
		}
	}

}