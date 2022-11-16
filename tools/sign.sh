# security find-identity -v -p codesigning

identity=$(security find-identity -v -p codesigning | grep 1\) | cut -d' ' -f4)

codesign --deep -f -v -s "$identity" TouchTableIRTrackingSystem.app