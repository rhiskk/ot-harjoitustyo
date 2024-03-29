package metuse.domain;

import java.util.ArrayList;
import java.util.List;
import metuse.dao.UserDao;

public class FakeUserDao implements UserDao {
    List<User> users = new ArrayList<>();
    int id;

    public FakeUserDao() {
        User u = new User("test", "test");
        u.setId(1);
        users.add(u);
        id = 2;
    }
    
    @Override
    public User findByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
    
    @Override
    public boolean create(User user) {
        for (User u : users) {
            if (u.getUsername().equals(user.getName())) {
                return false;
            }
        }
        setId(user);
        return true;
    }
    
    @Override
     public void setId(User user) {
         user.setId(id);
         id++;
         users.add(user);
     }
}