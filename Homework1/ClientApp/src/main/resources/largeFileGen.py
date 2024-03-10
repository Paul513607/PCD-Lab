def create_file(file_path, size_in_bytes):
    with open(file_path, 'wb') as file:
        file.write(b'\0' * size_in_bytes)

size_in_bytes = int(65534)

create_file("/home/paul/tempData/client/largeFile.txt", size_in_bytes)