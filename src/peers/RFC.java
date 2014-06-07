package peers;

public class RFC {
	
	private int number;
	
	private String title;
	
	private String peerHostName;

	public RFC(int num, String rfcName, String host) {
		this.number = num;
		this.title = rfcName;
		this.peerHostName = host;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPeerHostName() {
		return peerHostName;
	}

	public void setPeerHostName(String belongsToHost) {
		this.peerHostName = belongsToHost;
	}
	
}
