# 1501 Project 2 LZW
	Compresses a file with 3 methods of compression based on the LZW algorithm.
	*	Normal:		Which adjusts bit length as bits are written to the output file ranging from 9 to 16 bits.
	*	Reset:		Resets the codebook when completely full of 16 bit codes.
	*	Monitor:	Monitors the compression ratio and triggers a reset when the output compression ration 
				exceeds a certain limit.
				
# Known Bugs
	*Monitor seems to contain an edge case when using the code stored at 65535 (The last code aviable).
	Only appeared in Monitor but likely in Normal compression method as well. Reset should preform fine though.
