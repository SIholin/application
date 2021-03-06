package hifian.hintahaukka.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ShoppingCartDao {

    @Insert
    public void insert(Product product);

    @Delete
    public void delete(Product product);

    @Query("SELECT * FROM Products")
    LiveData<List<Product>> getProducts();

}
