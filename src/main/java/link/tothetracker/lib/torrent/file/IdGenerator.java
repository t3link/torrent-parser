package link.tothetracker.lib.torrent.file;

/**
 * @author t3link
 */
final class IdGenerator {

    private int id;

    public int add() {
        return ++this.id;
    }

}
