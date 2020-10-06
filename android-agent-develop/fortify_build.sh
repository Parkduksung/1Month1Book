#!/bin/bash
FORTIFY_BUILD_ID=rv_androidagnet
FORTIFY_VERSION_ID=RV_AndroidAgent
FORTIFY_ANALYZER=${FORTIFY_HOME}/bin/sourceanalyzer
export PATH=${PATH}:${GRADLE_HOME}/bin:${FORTIFY_HOME}/bin
gradle -version
sourceanalyzer -version
# clean
${FORTIFY_ANALYZER} -b ${FORTIFY_BUILD_ID} -clean
# build using fortify gradle intergration
${FORTIFY_ANALYZER} -b ${FORTIFY_BUILD_ID} gradle clean assembleAspRelease
 #pmd customFindbugs
# make task file for scan and upload
echo "${FORTIFY_BUILD_ID}" > ${FORTIFY_TOOL_DIR}/tasks/${FORTIFY_VERSION_ID}