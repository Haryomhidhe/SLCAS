package model;


public class UserAccount {
  private String userId; 
  private String name;
  private String email;

  public UserAccount(String userId, String name, String email){
     this.userId = userId;
     this.name = name;
     this.email = email;
  }

  //getter
public String getUserId(){
  return userId;
}
//setter
public void setUserId(String userId){
  this.userId = userId;
}
//getter
public String getName(){
  return name;
}
//setter
public void setName(String name){
  this.name = name;
}
//getter
public String getEmail(){
  return email;
}
//setter
public void setEmail(String email){
  this.email = email;
}
}
