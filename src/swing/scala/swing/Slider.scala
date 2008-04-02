package scala.swing

import event._
import Swing._

class Slider(override val peer: javax.swing.JSlider) extends Component with EditorComponent {
  def this() = this(new javax.swing.JSlider)
  
  def orientation: Orientation = Orientation.wrap(peer.getOrientation)
  def orientation_=(o: Orientation) { peer.setOrientation(o.peer) }
  
  def min: Int = peer.getMinimum
  def min_=(v: Int) { peer.setMinimum(v) }
  def max: Int = peer.getMaximum
  def max_=(v: Int) { peer.setMaximum(v) }
  def value: Int = peer.getValue
  def value_=(v: Int) { peer.setValue(v) }
  def extent: Int = peer.getExtent
  def extent_=(v: Int) { peer.setExtent(v) }
  
  def paintLabels: Boolean = peer.getPaintLabels
  def paintLabels_=(v: Boolean) { peer.setPaintLabels(v) }
  def paintTicks: Boolean = peer.getPaintTicks
  def paintTicks_=(v: Boolean) { peer.setPaintTicks(v) }
  def paintTrack: Boolean = peer.getPaintTrack
  def paintTrack_=(v: Boolean) { peer.setPaintTrack(v) }
  
  def snapToTicks: Boolean = peer.getSnapToTicks
  def snapToTicks_=(v: Boolean) { peer.setSnapToTicks(v) }
  
  def minorTickSpacing: Int = peer.getMinorTickSpacing
  def minorTickSpacing_=(v: Int) { peer.setMinorTickSpacing(v) }
  def majorTickSpacing: Int = peer.getMajorTickSpacing
  def majorTickSpacing_=(v: Int) { peer.setMajorTickSpacing(v) }
  
  def labels: collection.Map[Int, Label] = 
    new collection.jcl.MapWrapper[Int, Label] { def underlying = peer.getLabelTable.asInstanceOf[java.util.Hashtable[Int, Label]] }
  def labels_=(l: collection.Map[Int, Label]) {
    val table = new java.util.Hashtable[Any, Any]
    for ((k,v) <- l) table.put(k, v)
    peer.setLabelTable(table)
  }
  
  lazy val contentModified = new Publisher {
    peer.addChangeListener { ChangeListener( e => publish(ContentModified(Slider.this))) }
  }
}