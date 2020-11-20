#include <stdio.h>
#include <stdlib.h>


int
main(int argc, char* argv[])
{
	if (argc < 2)
	{
		perror("Usage: bytes_number [FILE]");
		exit(1);
	}
	
	// INPUT FILE
	FILE* in = fopen(argv[1], "rb");
	
	// OUTPUT FILE
	char output_filename[30];
	sprintf(output_filename, "%s.bn", argv[1]);
	FILE* out = fopen(output_filename, "w");

	// PRINT LOOP
	fprintf(out, "{ ");
	char current = (char)fgetc(in);
	while (current != EOF)
	{
		fprintf(out, "%u", (unsigned char)current);
		current = (char)fgetc(in);
		if (current != EOF) fprintf(out, ", ");
	}
	fprintf(out, " }");

	fclose(in);
	fclose(out);
	return 0;
}

