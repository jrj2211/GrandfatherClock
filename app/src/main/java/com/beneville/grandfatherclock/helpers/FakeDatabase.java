package com.beneville.grandfatherclock.helpers;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import com.beneville.grandfatherclock.database.AppDatabase;
import com.beneville.grandfatherclock.database.Song;

/**
 * Created by joeja on 1/30/2018.
 */

public class FakeDatabase {

    AppDatabase db;

    public FakeDatabase(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "database-name").build();

        syncSongs();
    }

    private void syncSongs() {
        final Song.Dao songDao = db.songDao();


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                songDao.deleteAll();
                songDao.insert(new Song("1904", "The Tallest Man on Earth", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("#40", "Dave Matthews", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("40oz to Freedom", "Sublime", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("#41", "Dave Matthews", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("American Girl", "Tom Petty", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("American Music", "Violent Femmes", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("American Pie", "Don McLean", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("And it Stoned Me", "Van Morrison", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("A Sailor's Christmas", "Jimmy Buffett", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Badfish", "Sublime", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Banana Pancakes", "Jack Johnson", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Barefoot Children", "Jimmy Buffett", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Big Parade", "The Lumineers", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Brown Eyed Girl", "Van Morrison", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Cape Canaveral", "Conor Oberst", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Carry On", "fun.", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Catch the Wind", "Donovan", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Cat's in the Cradle", "Harry Chapin", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Changes in Latitudes, Changes in Attitudes", "Jimmy Buffett", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Classy Girls", "The Lumineers", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Creep", "Radiohead", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Danny Boy", "Johnny Cash", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Darkness Between the Fireflies", "Mason Jennings", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Dead Sea", "The Lumineers", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Distantly in Love", "Jimmy Buffett", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Don't Leave Me (Ne Me Quitte Pas)", "Regina Spektor", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Don't Look Back in Anger", "Oasis", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Don't Stop Believin'", "Journey", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Doomsday", "Elvis Perkins", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Do You Remember", "Jack Johnson", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Drink the Water", "Jack Johnson", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Emmylou", "First Aid Kit", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Fall Line", "Jack Johnson", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Father and Son", "Cat Stevens", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Flake", "Jack Johnson", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Flapper Girl", "The Lumineers", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Flowers in Your Hair", "The Lumineers", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Folsom Prison Blues", "Johnny Cash", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Free Fallin'", "Tom Petty", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Furr", "Blitzen Trapper", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Get Well Cards", "Conor Oberst", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Gulf Coast Highway", "Emmylou Harris", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Half Light I", "Arcade Fire", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Half Light II (No Celebration)", "Arcade Fire", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Harvest", "Neil Young", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Heart of Gold", "Neil Young", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Here I Go Again", "Whitesnake", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Hey Jealousy", "Gin Blossoms", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Hey Soul Sister", "Train", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("High and Dry", "Radiohead", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Ho Hey", "The Lumineers", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Hollywood Forever Cemetery Sings", "Father John Misty", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Home", "Edward Sharpe & The Magnetic Zeros", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Honey Do", "Jimmy Buffett", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Hospitals and Jails", "Mason Jennings", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Hotel California", "The Eagles", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Hotel Yorba", "The White Stripes", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("I Feel Home", "OAR", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("I Knew You Were Trouble", "Taylor Swift", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("I'm Writing a Novel", "Father John Misty", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Island in the Sun", "Weezer", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("I Won't Give Up", "Jason Mraz", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Jack & Diane", "John Mellencamp", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Karma Police", "Radiohead", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("King of Spain", "The Tallest Man on Earth", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("King of the World", "First Aid Kit", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Lean On Me", "Bill Withers", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Little Talks", "Of Monsters and Men", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Live and Die", "The Avett Brothers", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Lola", "The Kinks", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Lonesome Town", "Ricky Nelson", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Love in the Library", "Jimmy Buffett", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Love Story", "Taylor Swift", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Margaritaville", "Jimmy Buffett", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Me and Julio Down by the Schoolyard", "Paul Simon", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Migration", "Jimmy Buffett", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Moonshadow", "Cat Stevens", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Mudfootball", "Jack Johnson", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("My Antonia", "Emmylou Harris", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("New Realization", "Sublime", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("No Surprises", "Radiohead", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Nothing", "Mason Jennings", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Nothing Else Matters", "Metallica", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Only Son of the Ladiesman", "Father John Misty", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Out on the Weekend", "Neil Young", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Party in the USA", "Miley Cyrus", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Patience", "Guns N' Roses", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Redemption Song", "Bob Marley", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Rivers of Babylon", "Sublime", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Rocket Man", "Elton John", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Rodeo Clowns", "Jack Johnson", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Send My Fond Regards to Lonelyville", "Elvis Perkins", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Sentimental Heart", "She & Him", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Shelter from the Storm", "Bob Dylan", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Some Nights", "fun.", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Somewhere Only We Know", "Keane", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Space Oddity", "David Bowie", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Stay or Leave", "Dave Matthews", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Stubborn Love", "The Lumineers", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Stuck in the Middle With You", "Stealers Wheel", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Submarines", "The Lumineers", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Sugar Mountain", "Neil Young", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Summer of '69", "Bryan Adams", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Sweet Home Alabama", "Lynyrd Skynyrd", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Tangled Up in Blue", "Bob Dylan", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("The Drugs Don't Work", "Ben Harper", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("The General", "Dispatch", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("The Lion's Roar", "First Aid Kit", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("The Man in Me", "Bob Dylan", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("The Mother We Share", "Chvrches", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("The Needle and the Damage Done", "Neil Young", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("The Weather is Here, Wish You Were Beautiful", "Jimmy Buffett", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("The Weight", "The Band", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Tin Cup Chalice", "Jimmy Buffett", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Tiny Dancer", "Elton John", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Trying to Reason with Hurricane Season", "Jimmy Buffett", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Under the Milky Way", "The Church", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Viva La Vida", "Coldplay", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Watching the Wheels", "John Lennon", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("Way Over Yonder in the Minor Key", "Billy Bragg & Wilco", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("We Are Never Ever Getting Back Together", "Taylor Swift", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("We're Going To Be Friends", "The White Stripes", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("What I Got", "Sublime", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("Wind and Walls", "The Tallest Man on Earth", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("With a Little Help From My Friends", "The Beatles", "", Song.ModeType.MOVIE, false));
                songDao.insert(new Song("Ya Hey", "Vampire Weekend", "", Song.ModeType.SONG, false));
                songDao.insert(new Song("You Belong With Me", "Taylor Swift", "", Song.ModeType.BOOK, false));
                songDao.insert(new Song("You Love Me", "DeVotchKa", "", Song.ModeType.SONG, false));
                return null;
            }

        }.execute();
    }
}
