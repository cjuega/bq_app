package com.cjuega.interviews.epub;

import java.io.IOException;

import com.cjuega.interviews.dropbox.DropboxManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class EPubHelper {
	public static Book openBookFromFileInfo(DbxFileInfo fileInfo) {
		DbxFile file = null;
		Book book = null;
		
		try{
			file = DropboxManager.getInstance().open(fileInfo.path);
			EpubReader reader = new EpubReader();
			book = reader.readEpub(file.getReadStream());
			
		} catch (DbxException e) {
			
		} catch (IOException e) {
			
		}finally{
			if (file != null)
			file.close();
		}
		
		return book;
	}
	
	public static Book openBookFromFile(DbxFile file) {
		Book book = null;
		
		try{
			book = (new EpubReader()).readEpub(file.getReadStream());
			
		} catch (DbxException e) {
			
		} catch (IOException e) {
			
		}finally{
			if (file != null)
			file.close();
		}
		
		return book;
	}
}
