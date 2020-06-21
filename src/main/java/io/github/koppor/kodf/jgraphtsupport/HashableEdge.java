package io.github.koppor.kodf.jgraphtsupport;

import java.util.Objects;
import org.jgrapht.graph.DefaultEdge;

public class HashableEdge extends DefaultEdge {

  @Override
  public int hashCode() {
    return Objects.hash(getSource(), getTarget());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    HashableEdge otherEdge = (HashableEdge) obj;
    return (Objects.equals(this.getSource(), otherEdge.getSource()))
        && Objects.equals(this.getTarget(), otherEdge.getTarget());
  }
}
