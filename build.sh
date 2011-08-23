javac -Xlint -cp ../Bukkit.jar mindless728/FluidFlow/*.java
jar cf FluidFlow.jar plugin.yml mindless728/FluidFlow/*.class
rm mindless728/FluidFlow/*.class
