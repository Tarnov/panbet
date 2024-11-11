#!/bin/sh
TMP_DIR=`pwd`"/tmp"
MD5_EXT="md5"
CLASS_EXT="class"

if [ "$2" = "" ]; then
    echo "Usage: `basename $0` <jar_for_verifying> <correct_jar>"
    exit 1
fi

# verifying jar
JAR1=$1
if [ ! -f $JAR1 ]; then
  echo "Cannot find file \"$JAR1\""
  exit 2
fi

# correct jar
JAR2=$2
if [ ! -f $JAR2 ]; then
  echo "Cannot find file \"$JAR2\""
  exit 2
fi


JAR1_TMP_DIR=$TMP_DIR/jar1
JAR2_TMP_DIR=$TMP_DIR/jar2

rm -rf $JAR1_TMP_DIR
mkdir -p $JAR1_TMP_DIR
unzip -q $JAR1 -d $JAR1_TMP_DIR

rm -rf $JAR2_TMP_DIR
mkdir -p $JAR2_TMP_DIR
unzip -q $JAR2 -d $JAR2_TMP_DIR

# remove all checksum from first jar files
find $JAR1_TMP_DIR -name "*.$MD5_EXT" -delete

# copy checksum from second jar file
MD5_LIST=`find $JAR2_TMP_DIR -name "*.$MD5_EXT" -printf %P\\\\n`
for MD5_FILE in $MD5_LIST;
do
  cp $JAR2_TMP_DIR/$MD5_FILE $JAR1_TMP_DIR/$MD5_FILE
done

CUR_DIR=`pwd`

# check hash values for class-files
CLASS_LIST=`find $JAR1_TMP_DIR -name "*.$CLASS_EXT" -printf %P\\\\n`
for CLASS_FILE in $CLASS_LIST;
do
  MD5_FILE="$JAR1_TMP_DIR/$CLASS_FILE.$MD5_EXT"
  if [ -f $MD5_FILE ]; then
    #cd `dirname $MD5_FILE`
    VERIFY_HASH=`md5sum $JAR1_TMP_DIR/$CLASS_FILE | awk '{print $1}'`
    CORRECT_HASH=`cat $MD5_FILE`
    if [ $VERIFY_HASH != $CORRECT_HASH ]; then
      echo "Verify failed for class \"$CLASS_FILE\""
    fi
  else
    # missing hash file, corresponding class file not present in correct jar
    echo "Class \"$CLASS_FILE\" is missing in correct jar \"$JAR2\", but present in in verifying jar \"$JAR1\""
  fi
done

echo ""

# check exist class-files for all hash files
CLASS_LIST=`find $JAR2_TMP_DIR -name "*.$CLASS_EXT" -printf %P\\\\n`
for CLASS_FILE in $CLASS_LIST;
do
  TARGET_CLASS_FILE="$JAR1_TMP_DIR/$CLASS_FILE"
  if [ ! -f $TARGET_CLASS_FILE ]; then
    echo "Class \"$CLASS_FILE\" is missing in verifying jar \"$JAR1\", but present in in correct jar \"$JAR2\""
  fi
done

cd $CUR_DIR

rm -rf $JAR1_TMP_DIR
rm -rf $JAR2_TMP_DIR