# Run this program (./bootstrap.sc) after changing any of
# the files that will affect the generation of the configure script

aclocal
autoheader
autoconf
touch README AUTHORS NEWS ChangeLog COPYING INSTALL
automake --add-missing
