package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface PublicationXref extends Xref
{
    // Property AUTHORS

    Set<String> getAuthor();

    void addAuthor(String author);

    void removeAuthor(String author);

    void setAuthor(Set<String> author);


    // Property SOURCE

    Set<String> getSource();

    void addSource(String source);

    void removeSource(String source);

    void setSource(Set<String> source);


    // Property TITLE

    String getTitle();

   void setTitle(String title);


    // Property URL

    Set<String> getUrl();

    void addUrl(String url);

    void removeUrl(String url);

    void setUrl(Set<String> url);


    // Property YEAR

    int getYear();

    void setYear(int year);
}
