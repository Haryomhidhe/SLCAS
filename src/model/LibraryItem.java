package model;
abstract class LibraryItem {
  private String id; // only id class can access it directly
  private String title; // only title class can access it directly
  private String author; // only author class can access it directly
  private int year; // only this year can access it directly
  private boolean isAvailable;  // only isAvailable class can access it directly

 public LibraryItem(String id, String title, String author, int year, boolean isAvailable ){
  this.id = id;
  this.title = title;
  this.author = author;
  this.year = year;
  this.isAvailable = isAvailable;
}
 
  public abstract String getItemType(); 

//getter
public String getId(){
  return id;
}
//setter
public void setId(String id){
  this.id = id;
}

//getter
public String getTitle(){
  return title;
}
//setter
public void setTitle(String title){
  this.title = title;
}

//getter
public String getAuthor(){
  return author;
}
//setter
public void setAuthor(String author){
  this.author = author;
}

//getter
public int getYear(){
  return year;
}
//setter
public void setYear(int year){
  this.year = year;
}

//getter
public boolean isAvailable(){
  return isAvailable;
}
//setter
public void setIsAvailable(boolean isAvailable){
  this.isAvailable = isAvailable;
}
}

