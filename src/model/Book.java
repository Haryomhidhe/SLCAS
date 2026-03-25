package model;
 class Book extends LibraryItem{

  private String genre;
  
  public Book (String id, String title, String author, int year, boolean isAvailable, String genre ){
    super(id,title,author,year,isAvailable); // send first 5 to parent
  this.genre = genre; // handle the extra one ourselves
}

public String getItemType(){
  return "Book";
}
//getter
public String getGenre(){
  return genre;
}
//setter
public void setGenre(String genre){
  this.genre = genre;
}
}
