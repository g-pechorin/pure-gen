@ECHO OFF


SET run=sbt "run -live -from=../peterlavalle-ex-swing.sbt"

CMD /C "CD ../gitout.sbt && %run%"
