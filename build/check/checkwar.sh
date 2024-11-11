#!/bin/sh
TMP_DIR=`pwd`"/tmp"

MD5_EXT="md5"
CLASS_EXT="class"
JS_EXT="js"

if [ "$2" = "" ]; then
    echo "Usage: `basename $0` {<path_to_verifying_webapp>|<verifying_war>}> <correct_war>"
    exit 1
fi

# verifying web content
WEB_DIR=$1
if [ ! -d $WEB_DIR -a ! -f $WEB_DIR ]; then
  echo "Cannot find directory \"$WEB_DIR\""
  exit 2
fi

# correct war
WAR=$2
if [ ! -f $WAR ]; then
  echo "Cannot find file \"$WAR\""
  exit 2
fi

WAR1_TMP_DIR=$TMP_DIR/war1

rm -rf $WAR1_TMP_DIR
mkdir -p $WAR1_TMP_DIR
if [ -d $WEB_DIR ]; then
  cp -r $WEB_DIR/* $WAR1_TMP_DIR
else
  unzip -q $WEB_DIR -d $WAR1_TMP_DIR
fi

WAR2_TMP_DIR=$TMP_DIR/war2

rm -rf $WAR2_TMP_DIR
mkdir -p $WAR2_TMP_DIR
unzip -q $WAR -d $WAR2_TMP_DIR

# remove all checksum from web files
find $WAR1_TMP_DIR -name "*.$MD5_EXT" -delete

# copy checksum from correct war
MD5_LIST=`find $WAR2_TMP_DIR -name "*.$MD5_EXT" -printf %P\\\\n`
for MD5_FILE in $MD5_LIST;
do
  cp $WAR2_TMP_DIR/$MD5_FILE $WAR1_TMP_DIR/$MD5_FILE
done

CUR_DIR=`pwd`

# check hash values for class-files
CLASS_LIST=`find $WAR1_TMP_DIR -name "*.$CLASS_EXT" -printf %P\\\\n`
for CLASS_FILE in $CLASS_LIST;
do
  MD5_FILE="$WAR1_TMP_DIR/$CLASS_FILE.$MD5_EXT"
  if [ -f $MD5_FILE ]; then
    #echo "Verify $MD5_FILE ..."
    VERIFY_HASH=`md5sum $WAR1_TMP_DIR/$CLASS_FILE | awk '{print $1}'`
    CORRECT_HASH=`cat $MD5_FILE`
    if [ $VERIFY_HASH != $CORRECT_HASH ]; then
      echo "Verify failed for class \"$CLASS_FILE\""
    fi
  else
    # missing hash file, corresponding class file not present in correct war
    echo "Class \"$CLASS_FILE\" is missing in correct war \"$WAR2\", but present in in verifying war \"$WAR1\""
  fi
done

echo ""

# check exist class-files for all hash files
CLASS_LIST=`find $WAR2_TMP_DIR -name "*.$CLASS_EXT" -printf %P\\\\n`
for CLASS_FILE in $CLASS_LIST;
do
  TARGET_CLASS_FILE="$WAR1_TMP_DIR/$CLASS_FILE"
  if [ ! -f $TARGET_CLASS_FILE ]; then
    echo "Class \"$CLASS_FILE\" is missing in verifying war \"$WAR1\", but present in in correct war \"$WAR2\""
  fi
done

echo ""

# check hash values for script-files
JS_LIST=`find $WAR1_TMP_DIR -name "*.$JS_EXT" -printf %P\\\\n`
for JS_FILE in $JS_LIST;
do
  MD5_FILE="$WAR1_TMP_DIR/$JS_FILE.$MD5_EXT"
  if [ -f $MD5_FILE ]; then
    #echo "Verify $MD5_FILE ..."
    VERIFY_HASH=`md5sum $WAR1_TMP_DIR/$JS_FILE | awk '{print $1}'`
    CORRECT_HASH=`cat $MD5_FILE`
    if [ $VERIFY_HASH != $CORRECT_HASH ]; then
      echo "Verify failed for script \"$JS_FILE\""
    fi
  else
    # missing hash file, corresponding file not present in correct war
    echo "Script \"$JS_FILE\" is missing in correct war \"$WAR2\", but present in in verifying war \"$WAR1\""
  fi
done

echo ""

# check exist class-files for all hash files
JS_LIST=`find $WAR2_TMP_DIR -name "*.$JS_EXT" -printf %P\\\\n`
for JS_FILE in $JS_LIST;
do
  TARGET_JS_FILE="$WAR1_TMP_DIR/$JS_FILE"
  if [ ! -f $TARGET_JS_FILE ]; then
    echo "Script \"$JS_FILE\" is missing in verifying war \"$WAR1\", but present in in correct war \"$WAR2\""
  fi
done

cd $CUR_DIR

#rm -rf $WAR1_TMP_DIR
#rm -rf $WAR2_TMP_DIR