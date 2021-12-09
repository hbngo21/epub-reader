# About

The repository is forked from the original [Folioreader-Android repo](https://github.com/FolioReader/FolioReader-Android). This repo focuses on working with taking notes feature.

### Features

- [x] Text Highlighting
- [x] List / Edit / Delete Highlights
- [x] Add / Edit Text Notes to a Highlight
- [x] Add / Edit Drawn Notes to a Highlight
- [x] Search on web and save as Notes to a Highlight
- [x] Clear Notes of a Highlight
- [x] Other features of the original project


### Usage

Get singleton object of `FolioReader`:

```java
FolioReader folioReader = FolioReader.get();
```

Call the function `openBook()`:

##### opening book from assets -

```java
folioReader.openBook("file:///android_asset/TheSilverChair.epub");
```
##### opening book from raw -

```java
folioReader.openBook(R.raw.accessible_epub_3);
```

### Credits
1. <a href="https://github.com/FolioReader/FolioReader-Android">Original authors</a>
2. <a href="https://github.com/daimajia/AndroidSwipeLayout">SwipeLayout</a>
3. <a href="https://github.com/readium/r2-streamer-kotlin">r2-streamer-kotlin</a>
4. <a href="http://developer.pearson.com/apis/dictionaries">Pearson Dictionaries</a>
5. <a href="https://github.com/timdown/rangy">rangy</a>

## License
The repo is available under the BSD license. See the [LICENSE](https://github.com/FolioReader/FolioReader-Android/blob/master/License.md) file.

