# Variables
JAVAC = javac
SERVER = TCPServer
CLIENT = TCPClient

# Compile all program files
all:
  $(JAVAC) $(SERVER).java $(CLIENT).java

# Remove compiled class
clean:
  rm -f *.class

# Run classes for the client and server
run-server:
  java $(SERVER)

run-client:
  java $(CLIENT)
