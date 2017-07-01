import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "Users")
public class User {
   String userName;
   String password;

   @Id
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
