package model;

class Journal extends LibraryItem{
     private int volume;
  
  public Journal (String id, String title, String author, int year, boolean isAvailable, int volume){
    super(id,title,author,year,isAvailable); // send first 5 to parent
  this.volume = volume; // handle the extra one ourselves
}

public String getItemType(){
  return "Journal";
}
//getter
public int getVolume(){
  return volume;
}
//setter
public void setVolume(int volume){
  this.volume = volume;
}
}

