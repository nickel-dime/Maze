import java.util.ArrayList;

// mutates the given list and returns a sorted list
class Quicksort<T extends Comparable<T>> {

  ArrayList<T> arr;

  Quicksort(ArrayList<T> arr) {
    this.arr = arr;
    this.quicksort();
  }

  // sorts list by weight
  // EFFECT: Sorts the given ArrayList according to the given comparator
  void quicksort() {
    quicksortHelp(0, arr.size());
  }

  // EFFECT: sorts the source array according to comp, in the range of indices [loIdx, hiIdx)
  private void quicksortHelp(int loIdx, int hiIdx) {
    // Step 0: check for completion
    if (loIdx >= hiIdx) {
      return; // There are no items to sort
    }
    // Step 1: select pivot
    T pivot = arr.get(loIdx);

    // Step 2: partition items to lower or upper portions of the temp list
    int pivotIdx = partition(loIdx, hiIdx, pivot);
    // Step 3: sort both halves of the list
    quicksortHelp(loIdx, pivotIdx);
    quicksortHelp(pivotIdx + 1, hiIdx);
  }

  // Returns the index where the pivot element ultimately ends up in the sorted source
  // EFFECT: Modifies the source list in the range [loIdx, hiIdx) such that
  // all values to the left of the pivot are less than (or equal to) the pivot
  // and all values to the right of the pivot are greater than it
  private int partition(int loIdx, int hiIdx, T pivot) {
    int curLo = loIdx;
    int curHi = hiIdx - 1;
    while (curLo < curHi) {
      // Advance curLo until we find a too-big value (or overshoot the end of the list)
      while (curLo < hiIdx && arr.get(curLo).compareTo(pivot) <= 0) {
        curLo = curLo + 1;
      }
      // Advance curHi until we find a too-small value (or undershoot the start of the list)
      while (curHi >= loIdx && arr.get(curHi).compareTo(pivot) > 0) {
        curHi = curHi - 1;
      }
      if (curLo < curHi) {
        swap(curLo, curHi);
      }
    }
    swap(loIdx, curHi); // place the pivot in the remaining spot
    return curHi;
  }

  // EFFECT: swaps two values in the arraylist at index1 and index2
  private void swap(int index1, int index2) {
    T temp = this.arr.get(index1);
    this.arr.set(index1, this.arr.get(index2));
    this.arr.set(index2, temp);
  }
}

