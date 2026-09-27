### W1 random access

| structure | operation | n | m | avg time (ms) | ns/op | metric | total | per op |
|---|---|---:|---:|---:|---:|---|---:|---:|
| DynamicArray | get | 100 | 10000 | 0.014 | 1.4 | accesses | 10000 | 1.0 |
| LinkedList | get | 100 | 10000 | 0.310 | 31.0 | accesses | 511327 | 51.1 |
| DynamicArray | get | 1000 | 10000 | 0.015 | 1.5 | accesses | 10000 | 1.0 |
| LinkedList | get | 1000 | 10000 | 4.134 | 413.4 | accesses | 5021262 | 502.1 |
| DynamicArray | get | 10000 | 10000 | 0.015 | 1.5 | accesses | 10000 | 1.0 |
| LinkedList | get | 10000 | 10000 | 43.914 | 4391.4 | accesses | 50188951 | 5018.9 |
| DynamicArray | get | 100000 | 10000 | 0.018 | 1.8 | accesses | 10000 | 1.0 |
| LinkedList | get | 100000 | 10000 | 451.461 | 45146.1 | accesses | 504940938 | 50494.1 |

### W2 search

| structure | operation | n | m | avg time (ms) | ns/op | metric | total | per op |
|---|---|---:|---:|---:|---:|---|---:|---:|
| DynamicArray | contains | 100 | 1000 | 0.028 | 27.8 | comparisons | 75375 | 75.4 |
| LinkedList | contains | 100 | 1000 | 0.075 | 75.0 | comparisons | 75375 | 75.4 |
| DynamicArray | contains | 1000 | 1000 | 0.218 | 217.9 | comparisons | 750836 | 750.8 |
| LinkedList | contains | 1000 | 1000 | 0.688 | 687.8 | comparisons | 750836 | 750.8 |
| DynamicArray | contains | 10000 | 1000 | 2.142 | 2141.6 | comparisons | 7529336 | 7529.3 |
| LinkedList | contains | 10000 | 1000 | 6.987 | 6987.0 | comparisons | 7529336 | 7529.3 |
| DynamicArray | contains | 100000 | 1000 | 20.619 | 20619.3 | comparisons | 73835543 | 73835.5 |
| LinkedList | contains | 100000 | 1000 | 68.001 | 68000.6 | comparisons | 73835543 | 73835.5 |

### W3 insert/remove

| structure | operation | n | m | avg time (ms) | ns/op | metric | total | per op |
|---|---|---:|---:|---:|---:|---|---:|---:|
| DynamicArray | insert_front | 100 | 1000 | 0.035 | 35.0 | accesses+moves | 602420 | 602.4 |
| DynamicArray | remove_front | 100 | 100 | 0.001 | 12.4 | accesses+moves | 5050 | 50.5 |
| LinkedList | insert_front | 100 | 1000 | 0.006 | 6.3 | accesses+moves | 1000 | 1.0 |
| LinkedList | remove_front | 100 | 100 | 0.000 | 3.2 | accesses+moves | 200 | 2.0 |
| DynamicArray | insert_middle | 100 | 1000 | 0.021 | 21.4 | accesses+moves | 302920 | 302.9 |
| DynamicArray | remove_middle | 100 | 100 | 0.001 | 8.8 | accesses+moves | 2550 | 25.5 |
| LinkedList | insert_middle | 100 | 1000 | 0.259 | 259.3 | accesses+moves | 301500 | 301.5 |
| LinkedList | remove_middle | 100 | 100 | 0.002 | 16.2 | accesses+moves | 2700 | 27.0 |
| DynamicArray | insert_front | 1000 | 1000 | 0.055 | 54.6 | accesses+moves | 1501524 | 1501.5 |
| DynamicArray | remove_front | 1000 | 1000 | 0.034 | 34.2 | accesses+moves | 500500 | 500.5 |
| LinkedList | insert_front | 1000 | 1000 | 0.008 | 7.8 | accesses+moves | 1000 | 1.0 |
| LinkedList | remove_front | 1000 | 1000 | 0.003 | 2.8 | accesses+moves | 2000 | 2.0 |
| DynamicArray | insert_middle | 1000 | 1000 | 0.030 | 30.5 | accesses+moves | 752024 | 752.0 |
| DynamicArray | remove_middle | 1000 | 1000 | 0.021 | 20.9 | accesses+moves | 250500 | 250.5 |
| LinkedList | insert_middle | 1000 | 1000 | 0.668 | 668.1 | accesses+moves | 751500 | 751.5 |
| LinkedList | remove_middle | 1000 | 1000 | 0.205 | 204.7 | accesses+moves | 252000 | 252.0 |
| DynamicArray | insert_front | 10000 | 1000 | 0.429 | 428.7 | accesses+moves | 10500500 | 10500.5 |
| DynamicArray | remove_front | 10000 | 1000 | 0.625 | 624.9 | accesses+moves | 9500500 | 9500.5 |
| LinkedList | insert_front | 10000 | 1000 | 0.007 | 7.2 | accesses+moves | 1000 | 1.0 |
| LinkedList | remove_front | 10000 | 1000 | 0.003 | 3.4 | accesses+moves | 2000 | 2.0 |
| DynamicArray | insert_middle | 10000 | 1000 | 0.226 | 226.3 | accesses+moves | 5251000 | 5251.0 |
| DynamicArray | remove_middle | 10000 | 1000 | 0.343 | 342.9 | accesses+moves | 4750500 | 4750.5 |
| LinkedList | insert_middle | 10000 | 1000 | 4.752 | 4751.7 | accesses+moves | 5251500 | 5251.5 |
| LinkedList | remove_middle | 10000 | 1000 | 4.200 | 4199.8 | accesses+moves | 4752000 | 4752.0 |
| DynamicArray | insert_front | 100000 | 1000 | 4.396 | 4396.0 | accesses+moves | 100500500 | 100500.5 |
| DynamicArray | remove_front | 100000 | 1000 | 6.269 | 6269.2 | accesses+moves | 99500500 | 99500.5 |
| LinkedList | insert_front | 100000 | 1000 | 0.005 | 4.8 | accesses+moves | 1000 | 1.0 |
| LinkedList | remove_front | 100000 | 1000 | 0.003 | 3.4 | accesses+moves | 2000 | 2.0 |
| DynamicArray | insert_middle | 100000 | 1000 | 2.151 | 2151.3 | accesses+moves | 50251000 | 50251.0 |
| DynamicArray | remove_middle | 100000 | 1000 | 3.107 | 3107.2 | accesses+moves | 49750500 | 49750.5 |
| LinkedList | insert_middle | 100000 | 1000 | 44.794 | 44794.5 | accesses+moves | 50251500 | 50251.5 |
| LinkedList | remove_middle | 100000 | 1000 | 43.745 | 43744.5 | accesses+moves | 49752000 | 49752.0 |

### W4 min-heap

| structure | operation | n | m | avg time (ms) | ns/op | metric | total | per op |
|---|---|---:|---:|---:|---:|---|---:|---:|
| MinHeap | insert | 100 | 100 | 0.001 | 5.8 | comparisons | 194 | 1.9 |
| MinHeap | peekMin | 100 | 10000 | 0.001 | 0.1 | comparisons | 0 | 0.0 |
| MinHeap | extractMin | 100 | 100 | 0.001 | 10.8 | comparisons | 841 | 8.4 |
| MinHeap | insert | 1000 | 1000 | 0.008 | 7.5 | comparisons | 2232 | 2.2 |
| MinHeap | peekMin | 1000 | 10000 | 0.001 | 0.1 | comparisons | 0 | 0.0 |
| MinHeap | extractMin | 1000 | 1000 | 0.025 | 24.6 | comparisons | 14994 | 15.0 |
| MinHeap | insert | 10000 | 10000 | 0.099 | 9.9 | comparisons | 22593 | 2.3 |
| MinHeap | peekMin | 10000 | 10000 | 0.001 | 0.1 | comparisons | 0 | 0.0 |
| MinHeap | extractMin | 10000 | 10000 | 0.484 | 48.4 | comparisons | 216736 | 21.7 |
| MinHeap | insert | 100000 | 100000 | 1.100 | 11.0 | comparisons | 227662 | 2.3 |
| MinHeap | peekMin | 100000 | 10000 | 0.001 | 0.1 | comparisons | 0 | 0.0 |
| MinHeap | extractMin | 100000 | 100000 | 6.362 | 63.6 | comparisons | 2831463 | 28.3 |

