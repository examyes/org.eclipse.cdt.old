OBJS = payroll.o
CC = g++
CFLAGS = -g -Wall

.cpp.o:
	$(CC) $(CFLAGS) -c $<

all: payroll

payroll: $(OBJS)
	$(CC) $(CFLAGS) $(OBJS) -o $@
#	g++ -g payroll.cpp -o payroll

payroll.o: payroll.cpp payclass.hpp payfunc.hpp

clean:
	rm -f core *.o payroll
