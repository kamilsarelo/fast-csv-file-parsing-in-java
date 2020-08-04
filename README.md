# Fast CSV-file parsing in Java

As part of a bigger side-project I am working on, I investigated several ways to improve the reading- and parsing-performance of CSV-files with specific schemas in Java.

## Reading

🚧👷 work in progress 👷🚧

### java.io.FileReader

```java
final LinkedList<String> lines = new LinkedList<>();
String line;
try (FileReader reader = new FileReader(path.toFile()); BufferedReader bufferedReader = new BufferedReader(reader)) {
	while ((line = bufferedReader.readLine()) != null) {
		lines.add(line);
	}
} catch (final Throwable t) {
	t.printStackTrace();
}
return lines;
```

### java.nio.file.Files.newBufferedReader()

```java
try (BufferedReader reader = Files.newBufferedReader(path)) {
	return reader.lines().collect(Collectors.toList());
} catch (final Throwable t) {
	t.printStackTrace();
}
return Collections.<String> emptyList();
```

### java.nio.file.Files.lines()

```java
try {
	return Files.lines(path).collect(Collectors.toList());
} catch (final Throwable t) {
	t.printStackTrace();
}
return Collections.<String> emptyList();
```

### java.nio.file.Files.readAllLines()

```java
try {
	return Files.readAllLines(path);
} catch (final Throwable t) {
	t.printStackTrace();
}
return Collections.<String> emptyList();
```

## Parsing

🚧👷 work in progress 👷🚧
