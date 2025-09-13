package com.weather.server.helper;

/*
 * write-ahead log for persistent  storage.
 * handle the file I/O and the logic for crash recovery, using text file
 * Only store PUT request because if the GET client dont need to be stored its request. 
 * Because if server crash the GET client will recognise this as communication fail and resend
 */
public class Storage {
   
}
