package org.mbach.homeautomation.story;

/**
 * Model class.
 *
 * @author Matthieu BACHELIER
 * @since 2017-05
 */
class Model {

    private final int id;
    private final String theContent;

    Model(String imageUri, int id) {
        this.id = id;
        theContent = imageUri;
    }

    String getTheContent() {
        return theContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        return id == model.id;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result;
        return result;
    }
}
