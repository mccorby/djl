/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.training.dataset;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import java.util.stream.Stream;

/**
 * Dataset wrapping {@link NDArray}s. It is able to combine multiple data and labels. Each sample
 * will be retrieved by indexing {@link NDArray}s along the first dimension.
 *
 * <p>The following is an example of how to use ArrayDataset:
 *
 * <pre>
 *     ArrayDataset.Builder builder = new Arrayset.Builder()
 *     ArrayDataset dataset = new ArrayDataset(
 *                      new NDArray[]{data1, data2},
 *                      new NDArray[]{label1, label2, label3},
 *                      new DataLoadingConfiguration.builder
 *                              .setBatchSize(20)
 *                              .build())
 * </pre>
 *
 * @see Dataset
 */
public class ArrayDataset extends RandomAccessDataset {

    protected NDArray[] data;
    protected NDArray[] labels;

    public ArrayDataset(BaseBuilder<?> builder) {
        super(builder);
        if (builder instanceof Builder) {
            Builder builder2 = (Builder) builder;
            data = builder2.getData();
            labels = builder2.getLabels();

            if (data != null && data.length != 0) {
                size = data[0].size(0);
            } else if (labels != null && labels.length != 0) {
                size = labels[0].size(0);
            }

            // check data and labels have the same size
            if (data != null && Stream.of(data).anyMatch(array -> array.size(0) != size)) {
                throw new IllegalArgumentException("All the NDArray must have the same length!");
            }
            if (labels != null && Stream.of(labels).anyMatch(array -> array.size(0) != size)) {
                throw new IllegalArgumentException("All the NDArray must have the same length!");
            }
        }
    }

    @Override
    public Record get(NDManager manager, long index) {
        NDList datum = new NDList();
        NDList label = new NDList();
        for (NDArray array : data) {
            datum.add(array.get(index));
        }
        if (labels != null) {
            for (NDArray array : labels) {
                label.add(array.get(index));
            }
        }
        datum.attach(manager);
        label.attach(manager);
        return new Record(datum, label);
    }

    @SuppressWarnings("rawtypes")
    public static final class Builder extends BaseBuilder<Builder> {

        private NDArray[] data;
        private NDArray[] labels;

        @Override
        protected Builder self() {
            return this;
        }

        public NDArray[] getData() {
            return data;
        }

        public Builder setData(NDArray data) {
            this.data = new NDArray[] {data};
            return self();
        }

        public Builder setData(NDArray[] data) {
            this.data = data;
            return self();
        }

        public NDArray[] getLabels() {
            return labels;
        }

        public Builder optLabels(NDArray labels) {
            this.labels = new NDArray[] {labels};
            return self();
        }

        public Builder optLabels(NDArray[] labels) {
            this.labels = labels;
            return self();
        }

        public ArrayDataset build() {
            return new ArrayDataset(this);
        }
    }
}