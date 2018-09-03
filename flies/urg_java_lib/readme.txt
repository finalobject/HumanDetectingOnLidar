About
=====
This small library is made to be useful start point to connect to Hokuyo's URG sensor.
It has support for Ethernet and serial connection devices. 
It implements the SCIP2.0 protocol.
Samples for Ethernet and Serial connection are included.
This project is meant for Netbeans IDE, however, it should work with others.

Serial connection
=================
This library uses the RXTX java library from http://mfizz.com/oss/rxtx-for-java
Please configure your classpath to refer to the right RXTXcomm.jar.
Also point the java.library.path to the DLL folder when running the Serial sample.
Example, for windows 32bits:
-Djava.library.path=.\lib\rxtx-2.1-7\Windows\x86

License
=======
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.