package hifian.hintahaukka.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class}, version = 2, exportSchema = false)
public abstract class ShoppingCartDatabase extends RoomDatabase {

    public abstract ShoppingCartDao shoppingCartDao();

    private static volatile ShoppingCartDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ShoppingCartDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ShoppingCartDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ShoppingCartDatabase.class, "shopping_cart_database")
                            .fallbackToDestructiveMigration()   // This means that when Database is updated, the data will be lost. Should be changed in the future.
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
