package model;

class Magazine extends LibraryItem {
    private int issueNumber;
  
  public Magazine (String id, String title, String author, int year, boolean isAvailable, int issueNumber ){
    super(id,title,author,year,isAvailable); // send first 5 to parent
  this.issueNumber = issueNumber; // handle the extra one ourselves
}

public String getItemType(){
  return "Magazine";
}
//getter
public int getIssueNumber(){
  return issueNumber;
}
//setter
public void setIssueNumber(int issueNumber){
  this.issueNumber = issueNumber;
}
}

