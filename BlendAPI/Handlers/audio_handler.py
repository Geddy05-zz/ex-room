
__author__ = 'Ali Lachhab'


class AudioProcess(object):
    def __init__(self):
        self.proc = ""

    def proc_kill(self):
        import subprocess
        self.proc = subprocess.Popen()

    def play_audio(self, *args, **kwargs):

        print("Playing audio now")
        print(args[0])


if __name__ == '__main__':
    # from optparse import OptionParser
    from argparse import ArgumentParser

    parser = ArgumentParser(usage="gebruik zo ... ",)

    # parser.add_argument("-p", "--play", type="int", help="Give me a integer as id", dest=play_audio)
    # parser.add_argument("--start", dest="start", action="store", type="int", default=False, help="Start the Database")
    parser.add_argument("--start", dest="start", type=int, action="store", help="Start the Database")

    parser.add_argument("--stop", dest="stop", type=int, action="store", help="stop the Database")
    parser.add_argument("--stop", dest="stop", action="store_true", default=False, help="Stop the proces with this argument")

    args = parser.parse_args()
    kv = vars(args)

    print(args)

    if args.start:
        play_audio(kv['start'])
    elif args.stop:
        stop_audio()
