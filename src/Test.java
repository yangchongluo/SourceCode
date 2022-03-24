import java.util.ArrayList;
import java.util.List;

public class Test {
    public static List<Integer> findKDistantIndices(int[] nums, int key, int k) {

        List<Integer> key_index_list = new ArrayList<>();
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < nums.length; i++) {

            if (nums[i] == key) {
                key_index_list.add(i);
            }
        }

        Integer[] key_index = new Integer[key_index_list.size()];

        key_index_list.toArray(key_index);

        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < key_index.length; j++) {

                if (Math.abs(i - key_index[j]) <= key && nums[key_index[j]] == key) {
                    if (result.size() == 0) {
                        result.add(i);
                    } else {
                        if (result.get(result.size() - 1) == i) {
                            continue;
                        } else {
                            result.add(i);
                        }

                    }

                } else {
                    continue;
                }
            }

        }

        return result;


    }

    public static void main(String[] args) {

        int[] nums = {2,2,2,2,2,2};

        findKDistantIndices(nums, 2, 2);
    }
}
