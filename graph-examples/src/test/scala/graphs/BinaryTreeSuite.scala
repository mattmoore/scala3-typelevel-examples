package graphs.binarytree

import weaver.*

object BinaryTreeSuite extends SimpleIOSuite {
  pureTest("Basic binary tree") {
    val intTree =
      Node(
        Node(
          Node(Leaf, 1, Leaf),
          2,
          Node(Leaf, 3, Leaf),
        ),
        4,
        Leaf,
      )

    expect.all(
      height(intTree) == 3,
    )
  }
}
