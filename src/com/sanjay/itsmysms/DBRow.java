package com.sanjay.itsmysms;

public class DBRow {

	public long time;
	public int sentrec, seen;
	public String phno, body;


	public DBRow(String newPhno, long newTime, int newSentRec, String newBody, int newSeen) {

		phno = newPhno;
		time = newTime;
		sentrec = newSentRec;
		body = newBody;
		seen = newSeen;
	}
	
	public DBRow() {

	}

	@Override
	public String toString() {

		String string;

		string = "Phone No.: " + phno + "\nTime: " + time + "\nSent/Rec: "
				+ sentrec + "\nBody: " + body;

		return string;
	}
}
